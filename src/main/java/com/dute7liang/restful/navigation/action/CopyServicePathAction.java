package com.dute7liang.restful.navigation.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.dute7liang.restful.common.ToolkitIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 复制“Go to Service”弹窗中当前选中的 REST 接口路径。
 */
public class CopyServicePathAction extends AnAction implements DumbAware {
    public static final String ACTION_ID = "RestfulSearch.CopyServicePath";
    private final RestServiceChooseByNamePopup targetPopup;

    public CopyServicePathAction() {
        this(null);
    }

    CopyServicePathAction(@Nullable RestServiceChooseByNamePopup targetPopup) {
        super("Copy Service Path", "Copy selected service path", ToolkitIcons.Copy);
        this.targetPopup = targetPopup;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        RestServiceChooseByNamePopup popup = getCurrentPopup(e.getProject());
        if (popup != null) {
            popup.copySelectedServicePath();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        RestServiceChooseByNamePopup popup = getCurrentPopup(e.getProject());
        e.getPresentation().setEnabled(popup != null && popup.hasSelectedServiceItem());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    private RestServiceChooseByNamePopup getCurrentPopup(Project project) {
        if (targetPopup != null) {
            return targetPopup;
        }
        if (project == null) {
            return null;
        }

        return project.getUserData(RestServiceChooseByNamePopup.CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY);
    }
}
