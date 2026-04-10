package com.dute7liang.restful.common.resolver;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.dute7liang.restful.method.RequestPath;
import com.dute7liang.restful.navigation.action.RestServiceItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * REST 服务解析器的基础抽象，统一处理模块、项目作用域和服务项构造。
 *
 * @author dute7liang
 */
public abstract class BaseServiceResolver implements ServiceResolver {
    Module myModule;
    Project myProject;

    /**
     * 在模块范围内收集全部服务项。
     *
     * @return 模块级服务项列表
     */
    @Override
    public List<RestServiceItem> findAllSupportedServiceItemsInModule() {
        List<RestServiceItem> itemList = new ArrayList<>();
        if (myModule == null) {
            return itemList;
        }

        GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(myModule);
        itemList = getRestServiceItemList(myModule.getProject(), globalSearchScope);
        return itemList;
    }

    /**
     * 由具体解析器实现实际的扫描逻辑。
     *
     * @param project 当前项目
     * @param globalSearchScope 当前搜索范围
     * @return 解析出的服务项列表
     */
    public abstract List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope);

    /**
     * 在项目范围内收集全部服务项。
     *
     * @return 项目级服务项列表
     */
    @Override
    public List<RestServiceItem> findAllSupportedServiceItemsInProject() {
        List<RestServiceItem> itemList = null;
        if (myProject == null && myModule != null) {
            myProject = myModule.getProject();
        }

        if (myProject == null) {
            return new ArrayList<>();
        }

        GlobalSearchScope globalSearchScope = GlobalSearchScope.projectScope(myProject);
        itemList = getRestServiceItemList(myProject, globalSearchScope);

        return itemList;
    }

    /**
     * 根据类路径和方法路径创建导航用的服务项。
     *
     * @param psiMethod 导航目标元素
     * @param classUriPath 类级路径
     * @param requestMapping 方法级路径和方法信息
     * @return 可导航的 REST 服务项
     */
    @NotNull
    protected RestServiceItem createRestServiceItem(PsiElement psiMethod, String classUriPath, RequestPath requestMapping) {
        if (!classUriPath.startsWith("/")) classUriPath = "/".concat(classUriPath);
        if (!classUriPath.endsWith("/")) classUriPath = classUriPath.concat("/");

        String methodPath = requestMapping.getPath();

        if (methodPath.startsWith("/")) methodPath = methodPath.substring(1, methodPath.length());
        String requestPath = classUriPath + methodPath;

        RestServiceItem item = new RestServiceItem(psiMethod, requestMapping.getMethod(), requestPath);
        if (myModule != null) {
            item.setModule(myModule);
        }
        return item;
    }
}
