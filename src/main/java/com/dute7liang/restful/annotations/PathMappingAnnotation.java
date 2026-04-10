package com.dute7liang.restful.annotations;

/**
 * 描述能够声明请求路径的注解元信息。
 *
 * @author dute7liang
 */
public interface PathMappingAnnotation {

    /**
     * 返回注解的完整限定名。
     *
     * @return 注解全限定类名
     */
    String getQualifiedName();

    /**
     * 返回注解的短名称。
     *
     * @return 注解短名称
     */
    String getShortName();
}
