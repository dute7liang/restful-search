/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dute7liang.restful.navigation.action;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.dute7liang.utils.ToolkitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 对 IntelliJ 原生 ChooseByNamePopup 的轻量包装，用于复用服务搜索弹窗实例并预处理输入内容。
 *
 * @author dute7liang
 */
public class RestServiceChooseByNamePopup extends ChooseByNamePopup {
    public static final Key<RestServiceChooseByNamePopup> CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY = new Key<>("ChooseByNamePopup");

    /**
     * 创建服务搜索弹窗实例。
     *
     * @param project 当前项目
     * @param model 搜索模型
     * @param provider 数据提供器
     * @param oldPopup 旧弹窗实例
     * @param predefinedText 预填文本
     * @param mayRequestOpenInCurrentWindow 是否允许在当前窗口打开
     * @param initialIndex 初始选中索引
     */
    protected RestServiceChooseByNamePopup(@Nullable Project project, @NotNull ChooseByNameModel model, @NotNull ChooseByNameItemProvider provider, @Nullable ChooseByNamePopup oldPopup, @Nullable String predefinedText, boolean mayRequestOpenInCurrentWindow, int initialIndex) {
        super(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
    }

    /**
     * 创建或复用一个服务搜索弹窗。
     *
     * @param project 当前项目
     * @param model 搜索模型
     * @param provider 数据提供器
     * @param predefinedText 预填文本
     * @param mayRequestOpenInCurrentWindow 是否允许在当前窗口打开
     * @param initialIndex 初始选中索引
     * @return 新的弹窗实例
     */
    public static RestServiceChooseByNamePopup createPopup(final Project project,
                                                           @NotNull final ChooseByNameModel model,
                                                           @NotNull ChooseByNameItemProvider provider,
                                                           @Nullable final String predefinedText,
                                                           boolean mayRequestOpenInCurrentWindow,
                                                           final int initialIndex) {
        if (!StringUtil.isEmptyOrSpaces(predefinedText)) {
            return new RestServiceChooseByNamePopup(project, model, provider, null, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);
        }

        final RestServiceChooseByNamePopup oldPopup = project == null ? null : project.getUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY);
        if (oldPopup != null) {
            oldPopup.close(false);
        }
        RestServiceChooseByNamePopup newPopup = new RestServiceChooseByNamePopup(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex);

        if (project != null) {
            project.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, newPopup);
        }
        return newPopup;
    }

    /**
     * 对用户输入的文本做统一转换。
     *
     * @param pattern 原始输入
     * @return 转换后的输入
     */
    @Override
    public String transformPattern(String pattern) {
        final ChooseByNameModel model = getModel();
        return getTransformedPattern(pattern, model);
    }

    /**
     * 根据模型类型决定是否清洗路径模式。
     *
     * @param pattern 原始输入
     * @param model 当前搜索模型
     * @return 处理后的输入模式
     */
    public static String getTransformedPattern(String pattern, ChooseByNameModel model) {
        if (!(model instanceof GotoRequestMappingModel)) {
            return pattern;
        }

        pattern = ToolkitUtil.removeRedundancyMarkup(pattern);
        return pattern;
    }

    /**
     * 从当前输入中提取成员名模式。
     *
     * @return `#` 后面的文本，不存在时返回 {@code null}
     */
    @Nullable
    public String getMemberPattern() {
        final String enteredText = getTrimmedText();
        final int index = enteredText.lastIndexOf('#');
        if (index == -1) {
            return null;
        }

        String name = enteredText.substring(index + 1).trim();
        return StringUtil.isEmptyOrSpaces(name) ? null : name;
    }
}
