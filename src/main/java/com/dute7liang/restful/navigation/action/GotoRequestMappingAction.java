package com.dute7liang.restful.navigation.action;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameFilter;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.dute7liang.restful.method.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.List;

/**
 * “Go to Service”入口动作，负责弹出 REST 路由搜索窗口。
 *
 * @author dute7liang
 */
public class GotoRequestMappingAction extends GotoActionBase implements DumbAware {
    /**
     * 响应动作触发，创建模型、过滤器和弹窗提供器并展示搜索弹窗。
     *
     * @param e 动作事件
     */
    @Override
    protected void gotoActionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.service");

        ChooseByNameContributor[] chooseByNameContributors = {
                new GotoRequestMappingContributor(e.getData(DataKeys.MODULE))
        };

        final GotoRequestMappingModel model = new GotoRequestMappingModel(project, chooseByNameContributors);

        GotoActionCallback<HttpMethod> callback = new GotoActionCallback<HttpMethod>() {
            @Override
            protected ChooseByNameFilter<HttpMethod> createFilter(@NotNull ChooseByNamePopup popup) {
                return new GotoRequestMappingFilter(popup, model, project);
            }

            @Override
            public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {
                if (element instanceof RestServiceItem) {
                    RestServiceItem navigationItem = (RestServiceItem) element;
                    if (navigationItem.canNavigate()) {
                        navigationItem.navigate(true);
                    }
                }
            }
        };

        GotoRequestMappingProvider provider = new GotoRequestMappingProvider(getPsiContext(e));
        showNavigationPopup(e, model, callback, "Request Mapping Url matching pattern", true, true, (ChooseByNameItemProvider) provider);
    }

    /**
     * 自定义展示弹窗的方式，并优先使用选中文本或剪贴板中的 URL 作为预填值。
     *
     * @param e 动作事件
     * @param model 搜索模型
     * @param callback 选择回调
     * @param findUsagesTitle 弹窗标题
     * @param useSelectionFromEditor 是否使用编辑器选中内容
     * @param allowMultipleSelection 是否允许多选
     * @param itemProvider 数据提供器
     * @param <T> 过滤值类型
     */
    @Override
    protected <T> void showNavigationPopup(AnActionEvent e,
                                           ChooseByNameModel model,
                                           final GotoActionCallback<T> callback,
                                           @Nullable final String findUsagesTitle,
                                           boolean useSelectionFromEditor,
                                           final boolean allowMultipleSelection,
                                           final ChooseByNameItemProvider itemProvider) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() && FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        Pair<String, Integer> start = getInitialText(useSelectionFromEditor, e);

        String copiedURL = tryFindCopiedURL();
        String predefinedText = start.first == null ? copiedURL : start.first;

        showNavigationPopup(callback, findUsagesTitle,
                RestServiceChooseByNamePopup.createPopup(project, model, itemProvider, predefinedText,
                        mayRequestOpenInCurrentWindow,
                        start.second), allowMultipleSelection);
    }

    /**
     * 尝试从剪贴板中提取一个可用的 URL 作为搜索预填值。
     *
     * @return URL 文本，未命中时返回 {@code null}
     */
    private String tryFindCopiedURL() {
        String contents = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
        if (contents == null) {
            return null;
        }

        contents = contents.trim();
        if (contents.startsWith("http")) {
            if (contents.length() <= 120) {
                return contents;
            } else {
                return contents.substring(0, 120);
            }
        }

        return null;
    }

    /**
     * REST 服务搜索弹窗的 HTTP 方法过滤器。
     */
    protected static class GotoRequestMappingFilter extends ChooseByNameFilter<HttpMethod> {
        /**
         * 创建方法过滤器。
         *
         * @param popup 当前弹窗
         * @param model 搜索模型
         * @param project 当前项目
         */
        GotoRequestMappingFilter(final ChooseByNamePopup popup, GotoRequestMappingModel model, final Project project) {
            super(popup, model, GotoRequestMappingConfiguration.getInstance(project), project);
        }

        /**
         * 返回全部可用的 HTTP 方法过滤项。
         *
         * @return HTTP 方法列表
         */
        @Override
        @NotNull
        protected List<HttpMethod> getAllFilterValues() {
            List<HttpMethod> elements = Arrays.asList(HttpMethod.values());
            return elements;
        }

        /**
         * 将过滤值转换为显示文本。
         *
         * @param value HTTP 方法
         * @return 显示文本
         */
        @Override
        protected String textForFilterValue(@NotNull HttpMethod value) {
            return value.name();
        }

        /**
         * 返回过滤项图标。
         *
         * @param value HTTP 方法
         * @return 当前未自定义，固定返回 {@code null}
         */
        @Override
        protected Icon iconForFilterValue(@NotNull HttpMethod value) {
            return null;
        }
    }
}
