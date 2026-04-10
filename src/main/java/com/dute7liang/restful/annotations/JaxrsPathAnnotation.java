package com.dute7liang.restful.annotations;

/**
 * 定义 JAX-RS 中用于声明路径的注解。
 *
 * @author dute7liang
 */
public enum JaxrsPathAnnotation implements PathMappingAnnotation {

    PATH("Path", "javax.ws.rs.Path");

    private String shortName;
    private String qualifiedName;

    /**
     * 创建一个 JAX-RS 路径注解定义。
     *
     * @param shortName 注解短名称
     * @param qualifiedName 注解全限定名
     */
    JaxrsPathAnnotation(String shortName, String qualifiedName) {
        this.shortName = shortName;
        this.qualifiedName = qualifiedName;
    }

    /**
     * 返回注解全限定名。
     *
     * @return 注解全限定名
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * 返回注解短名称。
     *
     * @return 注解短名称
     */
    public String getShortName() {
        return shortName;
    }
}
