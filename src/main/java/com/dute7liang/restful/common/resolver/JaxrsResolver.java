package com.dute7liang.restful.common.resolver;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.dute7liang.restful.annotations.JaxrsPathAnnotation;
import com.dute7liang.restful.common.jaxrs.JaxrsAnnotationHelper;
import com.dute7liang.restful.method.RequestPath;
import com.dute7liang.restful.navigation.action.RestServiceItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 负责扫描 JAX-RS 控制器并生成可导航的 REST 服务项。
 *
 * @author dute7liang
 */
public class JaxrsResolver extends BaseServiceResolver {

    /**
     * 创建模块级 JAX-RS 解析器。
     *
     * @param module 当前模块
     */
    public JaxrsResolver(Module module) {
        myModule = module;
    }

    /**
     * 创建项目级 JAX-RS 解析器。
     *
     * @param project 当前项目
     */
    public JaxrsResolver(Project project) {
        myProject = project;
    }

    /**
     * 扫描搜索范围内的 JAX-RS 类和方法，构建 REST 服务项列表。
     *
     * @param project 当前项目
     * @param globalSearchScope 当前搜索范围
     * @return 扫描得到的服务项列表
     */
    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(JaxrsPathAnnotation.PATH.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) continue;

            PsiClass psiClass = (PsiClass) psiElement;
            PsiMethod[] psiMethods = psiClass.getMethods();

            if (psiMethods == null) {
                continue;
            }

            String classUriPath = JaxrsAnnotationHelper.getClassUriPath(psiClass);

            for (PsiMethod psiMethod : psiMethods) {
                RequestPath[] methodUriPaths = JaxrsAnnotationHelper.getRequestPaths(psiMethod);

                for (RequestPath methodUriPath : methodUriPaths) {
                    RestServiceItem item = createRestServiceItem(psiMethod, classUriPath, methodUriPath);
                    itemList.add(item);
                }
            }
        }

        return itemList;
    }
}
