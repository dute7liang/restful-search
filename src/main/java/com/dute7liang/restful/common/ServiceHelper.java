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
 * 服务相关工具类
 */
public class ServiceHelper {
    public static final Logger LOG = Logger.getInstance(ServiceHelper.class);
    PsiMethod psiMethod;

    public ServiceHelper(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Module module) {

        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(module);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(module);
        ServiceResolver[] resolvers = {springResolver,jaxrsResolver};

        for (ServiceResolver resolver : resolvers) {
            List<RestServiceItem> allSupportedServiceItemsInModule = resolver.findAllSupportedServiceItemsInModule();

            itemList.addAll(allSupportedServiceItemsInModule);
        }

        return itemList;
    }

    @NotNull
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Project project) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(project);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(project);

        ServiceResolver[] resolvers = {springResolver,jaxrsResolver};
        for (ServiceResolver resolver : resolvers) {
            List<RestServiceItem> allSupportedServiceItemsInProject = resolver.findAllSupportedServiceItemsInProject();

            itemList.addAll(allSupportedServiceItemsInProject);
        }

        return itemList;
    }
}
