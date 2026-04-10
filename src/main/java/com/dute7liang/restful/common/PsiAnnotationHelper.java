package com.dute7liang.restful.common;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供对 PSI 注解属性值的统一读取能力。
 *
 * @author dute7liang
 */
public class PsiAnnotationHelper {
    /**
     * 读取注解某个属性上的全部值，兼容单值、数组值和引用表达式。
     *
     * @param annotation 目标注解
     * @param attr 属性名
     * @return 属性值列表
     */
    @NotNull
    public static List<String> getAnnotationAttributeValues(PsiAnnotation annotation, String attr) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attr);

        List<String> values = new ArrayList<>();
        // 只有注解
        // 一个值 class com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
        // 多个值 class com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression expression = (PsiReferenceExpression) value;
            values.add(expression.getText());
        } else if (value instanceof PsiLiteralExpression) {
            values.add(((PsiLiteralExpression) value).getValue().toString());
        } else if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();

            for (PsiAnnotationMemberValue initializer : initializers) {
                values.add(initializer.getText().replaceAll("\\\"", ""));
            }
        }

        return values;
    }

    /**
     * 读取注解某个属性的第一个值。
     *
     * @param annotation 目标注解
     * @param attr 属性名
     * @return 第一个属性值，不存在时返回 {@code null}
     */
    public static String getAnnotationAttributeValue(PsiAnnotation annotation, String attr) {
        List<String> values = getAnnotationAttributeValues(annotation, attr);
        if (!values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }
}
