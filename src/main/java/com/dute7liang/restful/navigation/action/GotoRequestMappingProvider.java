package com.dute7liang.restful.navigation.action;


import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.dute7liang.utils.ToolkitUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 自定义服务搜索数据提供器，负责在匹配前清洗用户输入的 URL。
 *
 * @author dute7liang
 */
public class GotoRequestMappingProvider extends DefaultChooseByNameItemProvider {

    /**
     * 复用父类的名称过滤逻辑。
     *
     * @param base 弹窗基类
     * @param names 全部候选名称
     * @param pattern 当前输入模式
     * @return 过滤后的名称列表
     */
    public List<String> filterNames(@NotNull ChooseByNameBase base, @NotNull String[] names, @NotNull String pattern) {
        return super.filterNames(base, names, pattern);
    }

    /**
     * 创建服务搜索提供器。
     *
     * @param context 当前上下文 PSI 元素
     */
    public GotoRequestMappingProvider(@Nullable PsiElement context) {
        super(context);
    }

    /**
     * 在真正匹配前移除 URL 中冗余部分，再交给父类继续处理。
     *
     * @param base 弹窗基类
     * @param pattern 用户输入模式
     * @param everywhere 是否全局搜索
     * @param indicator 进度指示器
     * @param consumer 结果消费器
     * @return 是否成功完成过滤
     */
    public boolean filterElements(@NotNull ChooseByNameBase base, @NotNull String pattern, boolean everywhere, @NotNull ProgressIndicator indicator, @NotNull Processor<Object> consumer) {
        pattern = ToolkitUtil.removeRedundancyMarkup(pattern);
        return super.filterElements(base, pattern, everywhere, indicator, consumer);
    }
}
