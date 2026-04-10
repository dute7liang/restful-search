package com.dute7liang.restful.common;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.dute7liang.restful.common.resolver.JaxrsResolver;
import com.dute7liang.restful.common.resolver.ServiceResolver;
import com.dute7liang.restful.common.resolver.SpringResolver;
import com.dute7liang.restful.navigation.action.RestServiceItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责协调各类解析器，汇总项目中的 REST 服务项。
 *
 * @author dute7liang
 */
public class ServiceHelper {
    public static final Logger LOG = Logger.getInstance(ServiceHelper.class);
    PsiMethod psiMethod;

    /**
     * 以 PSI 方法作为上下文创建帮助类。
     *
     * @param psiMethod 当前方法上下文
     */
    public ServiceHelper(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    /**
     * 基于指定模块收集全部可识别的 REST 服务项。
     *
     * @param module 当前模块
     * @return 模块级 REST 服务列表
     */
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Module module) {

        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(module);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(module);
        ServiceResolver[] resolvers = {springResolver, jaxrsResolver};

        for (ServiceResolver resolver : resolvers) {
            List<RestServiceItem> allSupportedServiceItemsInModule = resolver.findAllSupportedServiceItemsInModule();

            itemList.addAll(allSupportedServiceItemsInModule);
        }

        return itemList;
    }

    /**
     * 基于整个项目收集全部可识别的 REST 服务项。
     *
     * @param project 当前项目
     * @return 项目级 REST 服务列表
     */
    @NotNull
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Project project) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(project);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(project);

        ServiceResolver[] resolvers = {springResolver, jaxrsResolver};
        for (ServiceResolver resolver : resolvers) {
            List<RestServiceItem> allSupportedServiceItemsInProject = resolver.findAllSupportedServiceItemsInProject();

            itemList.addAll(allSupportedServiceItemsInProject);
        }

        return itemList;
    }
}
