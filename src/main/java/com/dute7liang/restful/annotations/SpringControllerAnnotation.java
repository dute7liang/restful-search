package com.dute7liang.restful.annotations;

/**
 * 定义 Spring 控制器类上会参与 REST 路由扫描的注解。
 *
 * @author dute7liang
 */
public enum SpringControllerAnnotation implements PathMappingAnnotation {

    CONTROLLER("Controller", "org.springframework.stereotype.Controller"),
    REST_CONTROLLER("RestController", "org.springframework.web.bind.annotation.RestController");

    private String shortName;
    private String qualifiedName;

    /**
     * 创建一个 Spring 控制器注解定义。
     *
     * @param shortName 注解短名称
     * @param qualifiedName 注解全限定名
     */
    SpringControllerAnnotation(String shortName, String qualifiedName) {
        this.shortName = shortName;
        this.qualifiedName = qualifiedName;
    }

    /**
     * 返回注解的全限定名。
     *
     * @return 注解全限定名
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * 返回注解的短名称。
     *
     * @return 注解短名称
     */
    public String getShortName() {
        return shortName;
    }
}
