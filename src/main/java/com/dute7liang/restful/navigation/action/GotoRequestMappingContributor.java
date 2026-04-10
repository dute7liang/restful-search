package com.dute7liang.restful.navigation.action;


import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.dute7liang.restful.common.ServiceHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 为“Go to Service”弹窗提供名称列表和导航项数据。
 *
 * @author dute7liang
 */
public class GotoRequestMappingContributor implements ChooseByNameContributor {
    Module myModule;

    private List<RestServiceItem> navItem;
    private String[] cachedNames = new String[0];
    private Project cachedProject;
    private boolean cachedOnlyThisModuleChecked;
    private boolean cacheLoaded;

    /**
     * 创建服务导航项提供者。
     *
     * @param myModule 当前模块，可为空
     */
    public GotoRequestMappingContributor(Module myModule) {
        this.myModule = myModule;
    }

    /**
     * 返回当前作用域内可导航的服务名称列表。
     *
     * @param project 当前项目
     * @param onlyThisModuleChecked 是否仅限制当前模块
     * @return 服务名称数组
     */
    @NotNull
    @Override
    public String[] getNames(Project project, boolean onlyThisModuleChecked) {
        if (cacheLoaded && cachedProject == project && cachedOnlyThisModuleChecked == onlyThisModuleChecked) {
            return cachedNames;
        }

        List<RestServiceItem> itemList;
        if (onlyThisModuleChecked && myModule != null) {
            itemList = ServiceHelper.buildRestServiceItemListUsingResolver(myModule);
        } else {
            itemList = ServiceHelper.buildRestServiceItemListUsingResolver(project);
        }

        navItem = itemList;
        cachedNames = new String[itemList.size()];
        for (int i = 0; i < itemList.size(); i++) {
            RestServiceItem requestMappingNavigationItem = itemList.get(i);
            cachedNames[i] = requestMappingNavigationItem.getName();
        }
        cachedProject = project;
        cachedOnlyThisModuleChecked = onlyThisModuleChecked;
        cacheLoaded = true;
        return cachedNames;
    }

    /**
     * 根据名称返回对应的导航项。
     *
     * @param name 当前匹配名称
     * @param pattern 用户输入模式
     * @param project 当前项目
     * @param onlyThisModuleChecked 是否仅限制当前模块
     * @return 导航项数组
     */
    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean onlyThisModuleChecked) {
        if (!cacheLoaded) {
            getNames(project, onlyThisModuleChecked);
        }
        NavigationItem[] navigationItems = navItem.stream().filter(item -> item.getName().equals(name)).toArray(NavigationItem[]::new);
        return navigationItems;

    }
}
