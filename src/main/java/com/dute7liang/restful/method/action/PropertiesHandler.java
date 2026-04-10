package com.dute7liang.restful.method.action;


import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// profile.active != null  // YamlPropertySourceLoader extends PropertySourceLoader .load
// PropertySourcesLoader.load 配置文件加载类
// 路径location：[file:./config/, file:./, classpath:/config/, classpath:/]
// 文件name：bootstrap，application
// 后缀：[properties, xml, yml, yaml]
// applicationConfig: [classpath:/application.yml]#prod
// 如果存在 activeProfile spring.profiles.active 存在，判断是否存在 application-activeProfile. 文件，
// 如果存在，判断是否存在设置，不存在则忽略
// 最终可能优先级 application.properties>application.yml>bootstrap.propertis>bootstrap.yml
// 路径：classpath:/(resource)>classpath:/config/

/**
 * 读取模块中的 Spring 配置文件，并解析端口、上下文路径等属性值。
 *
 * @author dute7liang
 */
public class PropertiesHandler {

    public List<String> CONFIG_FILES = Arrays.asList("application", "bootstrap");
    public List<String> FILE_EXTENSIONS = Arrays.asList("properties", "yml");

    String SPRING_PROFILE = "spring.profiles.active";

    String placeholderPrefix = "${";
    String valueSeparator = ":";
    String placeholderSuffix = "}";

    String activeProfile;

    Module module;

    /**
     * 返回当前支持的配置文件后缀，按优先级排序。
     *
     * @return 配置文件后缀数组
     */
    public String[] getFileExtensions() {
        return new String[]{"properties", "yml"};
    }

    /**
     * 返回当前支持的配置文件名，按优先级排序。
     *
     * @return 配置文件名数组
     */
    public String[] getConfigFiles() {
        return new String[]{"application", "bootstrap"};
    }

    /**
     * 为指定模块创建配置读取器。
     *
     * @param module 当前模块
     */
    public PropertiesHandler(Module module) {
        this.module = module;
    }

    /**
     * 读取服务端口配置。
     *
     * @return 服务端口，未配置时返回空字符串
     */
    public String getServerPort() {
        String port = null;
        String serverPortKey = "server.port";

        activeProfile = findProfilePropertyValue();

        if (activeProfile != null) {
            port = findPropertyValue(serverPortKey, activeProfile);
        }
        if (port == null) {
            port = findPropertyValue(serverPortKey, null);
        }

        return port != null ? port : "";
    }

    /**
     * 读取任意 Spring 配置属性。
     *
     * @param propertyKey 属性键
     * @return 属性值，未找到时返回空字符串
     */
    public String getProperty(String propertyKey) {
        String propertyValue = null;

        activeProfile = findProfilePropertyValue();

        if (activeProfile != null) {
            propertyValue = findPropertyValue(propertyKey, activeProfile);
        }
        if (propertyValue == null) {
            propertyValue = findPropertyValue(propertyKey, null);
        }

        return propertyValue != null ? propertyValue : "";
    }

    /**
     * 查找当前激活的 Spring Profile。
     *
     * @return 激活的 Profile
     */
    private String findProfilePropertyValue() {
        String activeProfile = findPropertyValue(SPRING_PROFILE, null);
        return activeProfile;
    }

    /**
     * 在配置文件中查找指定属性值。
     *
     * @param propertyKey 属性键
     * @param activeProfile 激活的 profile，可为空
     * @return 属性值
     */
    private String findPropertyValue(String propertyKey, String activeProfile) {
        String value = null;
        String profile = activeProfile != null ? "-" + activeProfile : "";
        for (String conf : getConfigFiles()) {
            for (String ext : getFileExtensions()) {
                String configFile = conf + profile + "." + ext;
                if (ext.equals("properties")) {
                    Properties properties = loadProertiesFromConfigFile(configFile);
                    if (properties != null) {
                        Object valueObj = properties.getProperty(propertyKey);
                        if (valueObj != null) {
                            value = cleanPlaceholderIfExist((String) valueObj);
                            return value;
                        }
                    }

                } else if (ext.equals("yml") || ext.equals("yaml")) {
                    Map<String, Object> propertiesMap = getPropertiesMapFromYamlFile(configFile);
                    if (propertiesMap != null) {
                        Object valueObj = propertiesMap.get(propertyKey);
                        if (valueObj == null) return null;

                        if (valueObj instanceof String) {
                            value = cleanPlaceholderIfExist((String) valueObj);
                        } else {
                            value = valueObj.toString();
                        }
                        return value;
                    }
                }
            }
        }

        return value;
    }

    /**
     * 从 properties 文件中加载配置对象。
     *
     * @param configFile 配置文件名
     * @return properties 对象
     */
    private Properties loadProertiesFromConfigFile(String configFile) {
        Properties properties = null;
        PsiFile applicationPropertiesFile = findPsiFileInModule(configFile);
        if (applicationPropertiesFile != null) {
            properties = loadPropertiesFromText(applicationPropertiesFile.getText());
        }
        return properties;
    }

    /**
     * 将文本内容加载为 {@link Properties}。
     *
     * @param text properties 文本
     * @return 解析后的 properties
     */
    @NotNull
    private Properties loadPropertiesFromText(String text) {
        Properties prop = new Properties();
        try {
            prop.load(new StringReader(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * 读取服务上下文路径。
     *
     * @return 上下文路径，未配置时返回空字符串
     */
    public String getContextPath() {
        String key = "server.context-path";
        String contextPath = null;

        activeProfile = findProfilePropertyValue();

        if (activeProfile != null) {
            contextPath = findPropertyValue(key, activeProfile);
        }
        if (contextPath == null) {
            contextPath = findPropertyValue(key, null);
        }

        return contextPath != null ? contextPath : "";

    }

    /**
     * 清理 Spring 占位符表达式中的默认值部分。
     *
     * @param value 原始配置值
     * @return 清理后的值
     */
    private String cleanPlaceholderIfExist(String value) {
        if (value != null && value.contains(placeholderPrefix) && value.contains(valueSeparator)) {
            String[] split = value.split(valueSeparator);

            if (split.length > 1) {
                value = split[1].replace(placeholderSuffix, "");
            }
        }
        return value;
    }

    /**
     * 从 YAML 配置文件中读取并压平配置。
     *
     * @param configFile 配置文件名
     * @return 压平后的配置映射
     */
    private Map<String, Object> getPropertiesMapFromYamlFile(String configFile) {
        PsiFile applicationPropertiesFile = findPsiFileInModule(configFile);
        if (applicationPropertiesFile != null) {
            Yaml yaml = new Yaml();

            String yamlText = applicationPropertiesFile.getText();
            try {
                Map<String, Object> ymlPropertiesMap = (Map<String, Object>) yaml.load(yamlText);
                return getFlattenedMap(ymlPropertiesMap);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 在模块范围内按文件名查找配置文件。
     *
     * @param fileName 文件名
     * @return 找到的 PSI 文件
     */
    private PsiFile findPsiFileInModule(String fileName) {
        PsiFile psiFile = null;
        PsiFile[] applicationProperties = FilenameIndex.getFilesByName(module.getProject(),
                fileName,
                GlobalSearchScope.moduleScope(module));

        if (applicationProperties.length > 0) {
            psiFile = applicationProperties[0];
        }

        return psiFile;
    }

    /**
     * 将嵌套的 YAML 结构压平成点路径映射。
     *
     * @param source 原始 YAML 映射
     * @return 压平后的映射
     */
    protected final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap();
        this.buildFlattenedMap(result, source, null);
        return result;
    }

    /**
     * 递归压平 Map / Collection 结构。
     *
     * @param result 结果映射
     * @param source 当前源映射
     * @param path 当前路径前缀
     */
    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        Iterator iterator = source.entrySet().iterator();

        while (true) {
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry) iterator.next();
                String key = entry.getKey();
                if (StringUtils.isNotBlank(path)) {
                    if (key.startsWith("[")) {
                        key = path + key;
                    } else {
                        key = path + '.' + key;
                    }
                }

                Object value = entry.getValue();
                if (value instanceof String) {
                    result.put(key, value);
                } else if (value instanceof Map) {
                    Map<String, Object> map = (Map) value;
                    this.buildFlattenedMap(result, map, key);
                } else if (value instanceof Collection) {
                    Collection<Object> collection = (Collection) value;
                    int count = 0;
                    Iterator var10 = collection.iterator();

                    while (var10.hasNext()) {
                        Object object = var10.next();
                        this.buildFlattenedMap(result, Collections.singletonMap("[" + count++ + "]", object), key);
                    }
                } else {
                    result.put(key, value != null ? value : "");
                }
            }

            return;
        }
    }
}
