package com.dute7liang.restful.common.jaxrs;


import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.dute7liang.restful.annotations.JaxrsHttpMethodAnnotation;
import com.dute7liang.restful.annotations.JaxrsPathAnnotation;
import com.dute7liang.restful.common.PsiAnnotationHelper;
import com.dute7liang.restful.method.RequestPath;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 负责解析 JAX-RS 注解中的类路径、方法路径和请求方法信息。
 *
 * @author dute7liang
 */
public class JaxrsAnnotationHelper {

    /**
     * 读取 JAX-RS {@code @Path} 注解上的 value 值。
     *
     * @param annotation 路径注解
     * @return 注解值，不存在时返回空字符串
     */
    private static String getWsPathValue(PsiAnnotation annotation) {
        String value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");

        return value != null ? value : "";
    }

    /**
     * 解析方法上的 JAX-RS 请求路径和请求方法组合。
     *
     * @param psiMethod 目标方法
     * @return 解析后的请求路径数组
     */
    public static RequestPath[] getRequestPaths(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
        if (annotations == null) return null;
        List<RequestPath> list = new ArrayList<>();

        PsiAnnotation wsPathAnnotation = psiMethod.getModifierList().findAnnotation(JaxrsPathAnnotation.PATH.getQualifiedName());
        String path = wsPathAnnotation == null ? psiMethod.getName() : getWsPathValue(wsPathAnnotation);

        JaxrsHttpMethodAnnotation[] jaxrsHttpMethodAnnotations = JaxrsHttpMethodAnnotation.values();

        Arrays.stream(annotations).forEach(a -> Arrays.stream(jaxrsHttpMethodAnnotations).forEach(methodAnnotation -> {
            if (a.getQualifiedName().equals(methodAnnotation.getQualifiedName())) {
                list.add(new RequestPath(path, methodAnnotation.getShortName()));
            }
        }));

        return list.toArray(new RequestPath[list.size()]);
    }

    /**
     * 获取类级别的 JAX-RS 路径。
     *
     * @param psiClass 目标类
     * @return 类级路径
     */
    public static String getClassUriPath(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getModifierList().findAnnotation(JaxrsPathAnnotation.PATH.getQualifiedName());

        String path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");

        return path != null ? path : "";
    }

    /**
     * 获取方法级的 JAX-RS 路径展示值。
     *
     * @param psiMethod 目标方法
     * @return 方法路径，未显式声明时回退为方法名
     */
    public static String getMethodUriPath(PsiMethod psiMethod) {
        JaxrsHttpMethodAnnotation requestAnnotation = null;

        List<JaxrsHttpMethodAnnotation> httpMethodAnnotations = Arrays.stream(JaxrsHttpMethodAnnotation.values()).filter(annotation ->
                psiMethod.getModifierList().findAnnotation(annotation.getQualifiedName()) != null
        ).collect(Collectors.toList());

        if (httpMethodAnnotations.size() > 0) {
            requestAnnotation = httpMethodAnnotations.get(0);
        }

        String mappingPath;
        if (requestAnnotation != null) {
            PsiAnnotation annotation = psiMethod.getModifierList().findAnnotation(JaxrsPathAnnotation.PATH.getQualifiedName());
            mappingPath = getWsPathValue(annotation);
        } else {
            String methodName = psiMethod.getName();
            mappingPath = StringUtils.uncapitalize(methodName);
        }

        return mappingPath;
    }
}
