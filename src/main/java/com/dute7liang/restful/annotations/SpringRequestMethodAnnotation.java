package com.dute7liang.restful.annotations;

/**
 * 定义 Spring Web 中支持的请求方法注解及其对应的 HTTP 方法。
 *
 * @author dute7liang
 */
public enum SpringRequestMethodAnnotation {

    REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping", null),
    GET_MAPPING("org.springframework.web.bind.annotation.GetMapping", "GET"),
    POST_MAPPING("org.springframework.web.bind.annotation.PostMapping", "POST"),
    PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping", "PUT"),
    DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping", "DELETE"),
    PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping", "PATCH");

    private String qualifiedName;
    private String methodName;

    /**
     * 创建一个 Spring 请求注解定义。
     *
     * @param qualifiedName 注解全限定名
     * @param methodName 对应的 HTTP 方法，通用映射时可为空
     */
    SpringRequestMethodAnnotation(String qualifiedName, String methodName) {
        this.qualifiedName = qualifiedName;
        this.methodName = methodName;
    }

    /**
     * 返回该注解直接对应的 HTTP 方法名称。
     *
     * @return HTTP 方法名，通用映射时可能为空
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
     * 根据全限定名查找对应的注解定义。
     *
     * @param qualifiedName 注解全限定名
     * @return 匹配到的注解定义，未匹配时返回 {@code null}
     */
    public static SpringRequestMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (SpringRequestMethodAnnotation springRequestAnnotation : SpringRequestMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    /**
     * 根据注解短名称查找对应的注解定义。
     *
     * @param requestMapping 注解短名称
     * @return 匹配到的注解定义，未匹配时返回 {@code null}
     */
    public static SpringRequestMethodAnnotation getByShortName(String requestMapping) {
        for (SpringRequestMethodAnnotation springRequestAnnotation : SpringRequestMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().endsWith(requestMapping)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }
}
