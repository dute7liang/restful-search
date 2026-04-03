package com.dute7liang.restful.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

public class MyToolwindowListener implements ToolWindowManagerListener {
    private final Project project;

    public MyToolwindowListener(Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        RestServicesNavigator.getInstance(project).stateChanged();
    }
}
