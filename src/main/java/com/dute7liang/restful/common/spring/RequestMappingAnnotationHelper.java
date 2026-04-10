package com.dute7liang.restful.common.spring;


import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.dute7liang.restful.annotations.SpringRequestMethodAnnotation;
import com.dute7liang.restful.common.PsiAnnotationHelper;
import com.dute7liang.restful.common.RestSupportedAnnotationHelper;
import com.dute7liang.restful.method.RequestPath;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 负责解析 Spring MVC / Spring Boot 中声明的请求路径与请求方法。
 *
 * @author dute7liang
 */
public class RequestMappingAnnotationHelper implements RestSupportedAnnotationHelper {

    /**
     * 解析类级别的请求路径配置。
     *
     * @param psiClass 目标类
     * @return 类级请求路径列表
     */
    public static List<RequestPath> getRequestPaths(PsiClass psiClass) {
        if (psiClass == null || psiClass.getModifierList() == null) {
            return new ArrayList<>(Collections.singletonList(new RequestPath("/", null)));
        }

        PsiAnnotation[] annotations = psiClass.getModifierList().getAnnotations();
        if (annotations == null) {
            return new ArrayList<>(Collections.singletonList(new RequestPath("/", null)));
        }

        PsiAnnotation requestMappingAnnotation = null;
        List<RequestPath> list = new ArrayList<>();
        for (PsiAnnotation annotation : annotations) {
            for (SpringRequestMethodAnnotation mappingAnnotation : SpringRequestMethodAnnotation.values()) {
                if (annotation.getQualifiedName().equals(mappingAnnotation.getQualifiedName())) {
                    requestMappingAnnotation = annotation;
                }
            }
        }

        if (requestMappingAnnotation != null) {
            List<RequestPath> requestMappings = getRequestMappings(requestMappingAnnotation, "");
            if (requestMappings.size() > 0) {
                list.addAll(requestMappings);
            }
        } else {
            // TODO : 继承 RequestMapping
            PsiClass superClass = psiClass.getSuperClass();
            String superQualifiedName = superClass == null ? null : superClass.getQualifiedName();
            if (superClass != null && superQualifiedName != null && !"java.lang.Object".equals(superQualifiedName)) {
                list = getRequestPaths(superClass);
            } else {
                list.add(new RequestPath("/", null));
            }
        }

        return list;
    }

    /**
     * 获取类级 {@code @RequestMapping} 的原始路径数组。
     *
     * @param psiClass 目标类
     * @return 路径数组
     */
    public static String[] getRequestMappingValues(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getModifierList().getAnnotations();
        if (annotations == null) return null;

        for (PsiAnnotation annotation : annotations) {
            if (annotation.getQualifiedName().equals(SpringRequestMethodAnnotation.REQUEST_MAPPING.getQualifiedName())) {
                return getRequestMappingValues(annotation);
            }
        }

        return new String[]{"/"};
    }

    /**
     * 将一个 Spring 请求注解解析为路径与方法的组合列表。
     *
     * @param annotation 目标注解
     * @param defaultValue 默认路径值
     * @return 请求路径组合列表
     */
    private static List<RequestPath> getRequestMappings(PsiAnnotation annotation, String defaultValue) {
        List<RequestPath> mappingList = new ArrayList<>();

        SpringRequestMethodAnnotation requestAnnotation = SpringRequestMethodAnnotation.getByQualifiedName(annotation.getQualifiedName());

        if (requestAnnotation == null) {
            return new ArrayList<>();
        }

        List<String> methodList;
        if (requestAnnotation.methodName() != null) {
            methodList = Arrays.asList(requestAnnotation.methodName());
        } else {
            methodList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "method");
        }

        List<String> pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "value");
        if (pathList.size() == 0) {
            pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path");
        }

        if (pathList.size() == 0) {
            pathList.add(defaultValue);
        }

        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    mappingList.add(new RequestPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                mappingList.add(new RequestPath(path, null));
            }
        }

        return mappingList;
    }

    /**
     * 解析方法级别的请求路径配置。
     *
     * @param psiMethod 目标方法
     * @return 请求路径数组
     */
    public static RequestPath[] getRequestPaths(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();

        if (annotations == null) return null;
        List<RequestPath> list = new ArrayList<>();

        for (PsiAnnotation annotation : annotations) {
            for (SpringRequestMethodAnnotation mappingAnnotation : SpringRequestMethodAnnotation.values()) {
                if (mappingAnnotation.getQualifiedName().equals(annotation.getQualifiedName())) {
                    String defaultValue = "/";
                    List<RequestPath> requestMappings = getRequestMappings(annotation, defaultValue);
                    if (requestMappings.size() > 0) {
                        list.addAll(requestMappings);
                    }
                }
            }
        }

        return list.toArray(new RequestPath[list.size()]);
    }

    /**
     * 读取请求映射注解中的单个路径值。
     *
     * @param annotation 目标注解
     * @return 路径值
     */
    private static String getRequestMappingValue(PsiAnnotation annotation) {
        String value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");

        if (StringUtils.isEmpty(value)) {
            value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "path");
        }
        return value;
    }

    /**
     * 读取请求映射注解中的全部路径值。
     *
     * @param annotation 目标注解
     * @return 路径值数组
     */
    public static String[] getRequestMappingValues(PsiAnnotation annotation) {
        String[] values;
        PsiAnnotationMemberValue attributeValue = annotation.findDeclaredAttributeValue("value");

        if (attributeValue instanceof PsiLiteralExpression) {
            return new String[]{((PsiLiteralExpression) attributeValue).getValue().toString()};
        }
        if (attributeValue instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) attributeValue).getInitializers();
            values = new String[initializers.length];

            for (int i = 0; i < initializers.length; i++) {
                values[i] = ((PsiLiteralExpression) (initializers[i])).getValue().toString();
            }
        }

        return new String[]{};
    }

    /**
     * 获取类级单一路径，主要用于展示或简化处理。
     *
     * @param psiClass 目标类
     * @return 单一路径
     */
    public static String getOneRequestMappingPath(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getModifierList().findAnnotation(SpringRequestMethodAnnotation.REQUEST_MAPPING.getQualifiedName());

        String path = null;
        if (annotation != null) {
            path = RequestMappingAnnotationHelper.getRequestMappingValue(annotation);
        }

        return path != null ? path : "";
    }

    /**
     * 获取方法级单一路径，未显式声明时使用方法名兜底。
     *
     * @param psiMethod 目标方法
     * @return 单一路径
     */
    public static String getOneRequestMappingPath(PsiMethod psiMethod) {
        SpringRequestMethodAnnotation requestAnnotation = null;

        List<SpringRequestMethodAnnotation> springRequestAnnotations = Arrays.stream(SpringRequestMethodAnnotation.values()).filter(annotation ->
                psiMethod.getModifierList().findAnnotation(annotation.getQualifiedName()) != null
        ).collect(Collectors.toList());

        if (springRequestAnnotations.size() > 0) {
            requestAnnotation = springRequestAnnotations.get(0);
        }

        String mappingPath;
        if (requestAnnotation != null) {
            PsiAnnotation annotation = psiMethod.getModifierList().findAnnotation(requestAnnotation.getQualifiedName());
            mappingPath = RequestMappingAnnotationHelper.getRequestMappingValue(annotation);
        } else {
            String methodName = psiMethod.getName();
            mappingPath = StringUtils.uncapitalize(methodName);
        }

        return mappingPath;
    }
}
