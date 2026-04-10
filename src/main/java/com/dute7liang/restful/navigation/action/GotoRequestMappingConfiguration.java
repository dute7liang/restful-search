package com.dute7liang.restful.navigation.action;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.dute7liang.restful.method.HttpMethod;

/**
 * 保存“Go to Service”弹窗中 HTTP 方法过滤器的勾选状态。
 *
 * @author dute7liang
 */
@State(name = "GotoRequestMappingConfiguration", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
class GotoRequestMappingConfiguration extends ChooseByNameFilterConfiguration<HttpMethod> {
    /**
     * 获取当前项目对应的过滤配置实例。
     *
     * @param project 当前项目
     * @return 过滤配置实例
     */
    public static GotoRequestMappingConfiguration getInstance(Project project) {
        return project.getService(GotoRequestMappingConfiguration.class);
    }

    /**
     * 将过滤元素转换为持久化用的名称。
     *
     * @param type HTTP 方法枚举
     * @return 方法名称
     */
    @Override
    protected String nameForElement(HttpMethod type) {
        return type.name();
    }
}
