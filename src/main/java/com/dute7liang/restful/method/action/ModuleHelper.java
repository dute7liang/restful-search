package com.dute7liang.restful.method.action;


import com.intellij.openapi.module.Module;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 根据模块配置推导服务访问前缀，例如协议、端口和上下文路径。
 *
 * @author dute7liang
 */
public class ModuleHelper {
    Module module;

    // URL
    private static final String SCHEME = "http://"; // PROTOCOL
    private static final String HOST = "localhost";
    private static final String PORT = "8080"; // int
    public static String DEFAULT_URI = "http://localhost" + ":" + PORT;

    PropertiesHandler propertiesHandler;

    /**
     * 预留的 authority 获取入口。
     *
     * @return 当前未实现，固定返回 {@code null}
     */
    public static String getAUTHORITY() {
        return null;
    }

    /**
     * 为指定模块创建 URL 辅助对象。
     *
     * @param module 当前模块
     */
    public ModuleHelper(Module module) {
        this.module = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    /**
     * 快速创建模块辅助对象。
     *
     * @param module 当前模块
     * @return 辅助对象实例
     */
    public static ModuleHelper create(Module module) {
        return new ModuleHelper(module);
    }

    /**
     * 计算当前模块服务的主机前缀。
     *
     * @return 形如 {@code http://localhost:8080/context} 的前缀
     */
    @NotNull
    public String getServiceHostPrefix() {
        if (module == null) {
            return DEFAULT_URI;
        }

        String port = propertiesHandler.getServerPort();
        if (StringUtils.isEmpty(port)) port = PORT;

        String contextPath = propertiesHandler.getContextPath();
        return new StringBuilder(SCHEME).append(HOST).append(":").append(port).append(contextPath).toString();
    }
}
