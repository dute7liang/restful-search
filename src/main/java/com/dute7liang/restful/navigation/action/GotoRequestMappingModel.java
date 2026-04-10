package com.dute7liang.restful.navigation.action;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.gotoByName.CustomMatcherModel;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.dute7liang.restful.common.spring.AntPathMatcher;
import com.dute7liang.restful.method.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * “Go to Service”弹窗的数据模型，负责提示文案、过滤器和匹配规则。
 *
 * @author dute7liang
 */
public class GotoRequestMappingModel extends FilteringGotoByModel<HttpMethod> implements DumbAware, CustomMatcherModel {

    /**
     * 创建服务搜索弹窗模型。
     *
     * @param project 当前项目
     * @param contributors 名称贡献者列表
     */
    protected GotoRequestMappingModel(@NotNull Project project, @NotNull ChooseByNameContributor[] contributors) {
        super(project, contributors);
    }

    /**
     * 从导航项中提取过滤值，用于 HTTP 方法过滤器。
     *
     * @param item 当前导航项
     * @return 当前项对应的 HTTP 方法
     */
    @Nullable
    @Override
    protected HttpMethod filterValueFor(NavigationItem item) {
        if (item instanceof RestServiceItem) {
            return ((RestServiceItem) item).getMethod();
        }

        return null;
    }

    /**
     * 返回当前可用的过滤项集合。
     *
     * @return 过滤项集合
     */
    @Nullable
    @Override
    protected synchronized Collection<HttpMethod> getFilterItems() {
        return super.getFilterItems();
    }

    /**
     * 返回弹窗输入提示。
     *
     * @return 提示文本
     */
    @Override
    public String getPromptText() {
        return "Enter service URL path :";
    }

    /**
     * 返回在指定作用域中未命中结果时的提示。
     *
     * @return 未命中提示
     */
    @Override
    public String getNotInMessage() {
        return IdeBundle.message("label.no.matches.found.in.project");
    }

    /**
     * 返回完全未命中结果时的提示。
     *
     * @return 未找到提示
     */
    @Override
    public String getNotFoundMessage() {
        return IdeBundle.message("label.no.matches.found");
    }

    /**
     * 返回模块过滤复选框的快捷键。
     *
     * @return 复选框助记键
     */
    @Override
    public char getCheckBoxMnemonic() {
        return SystemInfo.isMac ? 'P' : 'n';
    }

    /**
     * 读取“仅当前模块”复选框的初始状态。
     *
     * @return 初始勾选状态
     */
    @Override
    public boolean loadInitialCheckBoxState() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);
        return propertiesComponent.isTrueValue("GoToRestService.OnlyCurrentModule");
    }

    /**
     * 保存“仅当前模块”复选框的状态。
     *
     * @param state 当前勾选状态
     */
    @Override
    public void saveInitialCheckBoxState(boolean state) {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);
        if (propertiesComponent.isTrueValue("GoToRestService.OnlyCurrentModule")) {
            propertiesComponent.setValue("GoToRestService.OnlyCurrentModule", Boolean.toString(state));
        }
    }

    /**
     * 返回元素的完整名称，用于名称匹配。
     *
     * @param element 目标元素
     * @return 元素完整名称
     */
    @Nullable
    @Override
    public String getFullName(Object element) {
        return getElementName(element);
    }

    /**
     * 指定路径匹配时使用的分隔符。
     *
     * @return 分隔符数组
     */
    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[]{"/", "?"};
    }

    /**
     * 返回“仅当前模块”复选框名称。
     *
     * @return 复选框名称
     */
    @Nullable
    @Override
    public String getCheckBoxName() {
        return "Only This Module";
    }

    /**
     * 指定命中结果后是否打开编辑器。
     *
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean willOpenEditor() {
        return true;
    }

    /**
     * 自定义服务路径匹配规则，支持模糊匹配和 Ant 风格路径匹配。
     *
     * @param popupItem 候选项文本
     * @param userPattern 用户输入模式
     * @return 是否匹配成功
     */
    @Override
    public boolean matches(@NotNull String popupItem, @NotNull String userPattern) {
        String pattern = userPattern;
        if (pattern.equals("/")) return true;
        // REST风格的参数  @RequestMapping(value="{departmentId}/employees/{employeeId}")  PathVariable
        // REST风格的参数（正则） @RequestMapping(value="/{textualPart:[a-z-]+}.{numericPart:[\\d]+}")  PathVariable
        MinusculeMatcher matcher = NameUtil.buildMatcher("*" + pattern, NameUtil.MatchingCaseSensitivity.NONE);
        boolean matches = matcher.matches(popupItem);
        if (!matches) {
            AntPathMatcher pathMatcher = new AntPathMatcher();
            matches = pathMatcher.match(popupItem, userPattern);
        }
        return matches;
    }

    /**
     * 返回去除模型专属标记后的文本。
     *
     * @param pattern 原始模式
     * @return 处理后的模式
     */
    @NotNull
    @Override
    public String removeModelSpecificMarkup(@NotNull String pattern) {
        return super.removeModelSpecificMarkup(pattern);
    }

    /**
     * 返回结果列表的渲染器。
     *
     * @return 列表渲染器
     */
    @Override
    public ListCellRenderer getListCellRenderer() {
        return super.getListCellRenderer();
    }
}
