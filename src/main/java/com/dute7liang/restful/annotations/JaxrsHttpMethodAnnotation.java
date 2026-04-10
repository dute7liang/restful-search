package com.dute7liang.restful.annotations;

/**
 * 定义 JAX-RS 中支持的 HTTP 方法注解。
 *
 * @author dute7liang
 */
public enum JaxrsHttpMethodAnnotation {
    GET("javax.ws.rs.GET", "GET"),
    POST("javax.ws.rs.POST", "POST"),
    PUT("javax.ws.rs.PUT", "PUT"),
    DELETE("javax.ws.rs.DELETE", "DELETE"),
    HEAD("javax.ws.rs.HEAD", "HEAD"),
    PATCH("javax.ws.rs.PATCH", "PATCH");

    private String qualifiedName;
    private String methodName;

    /**
     * 创建一个 JAX-RS HTTP 方法注解定义。
     *
     * @param qualifiedName 注解全限定名
     * @param methodName 对应的 HTTP 方法名
     */
    JaxrsHttpMethodAnnotation(String qualifiedName, String methodName) {
        this.qualifiedName = qualifiedName;
        this.methodName = methodName;
    }

    /**
     * 返回对应的 HTTP 方法名。
     *
     * @return HTTP 方法名
     */
    public String methodName() {
        return this.methodName;
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
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") - 1);
    }

    /**
     * 根据全限定名查找注解定义。
     *
     * @param qualifiedName 注解全限定名
     * @return 匹配到的注解定义，未匹配时返回 {@code null}
     */
    public static JaxrsHttpMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (JaxrsHttpMethodAnnotation springRequestAnnotation : JaxrsHttpMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }
}
