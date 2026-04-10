package com.dute7liang.utils;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.DisposeAwareRunnable;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * 提供插件运行期常用的 IntelliJ 平台辅助能力。
 *
 * @author dute7liang
 */
public class ToolkitUtil {
    /**
     * 在项目初始化完成后执行指定任务。
     *
     * @param project 当前项目
     * @param r 待执行任务
     */
    public static void runWhenInitialized(final Project project, final Runnable r) {

        if (project.isDisposed()) {
            return;
        }

        if (isNoBackgroundMode()) {
            r.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project));
            return;
        }
        invokeLater(project, r);
    }

    /**
     * 在项目进入 smart mode 后执行任务。
     *
     * @param project 当前项目
     * @param runnable 待执行任务
     */
    public static void runWhenProjectIsReady(final Project project, final Runnable runnable) {
        DumbService.getInstance(project).smartInvokeLater(runnable);
    }

    /**
     * 判断当前是否处于无后台任务模式。
     *
     * @return 处于测试或 headless 环境时返回 {@code true}
     */
    public static boolean isNoBackgroundMode() {
        return (ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment());
    }

    /**
     * 按照 dumb-aware 规则执行任务。
     *
     * @param project 当前项目
     * @param r 待执行任务
     */
    public static void runDumbAware(final Project project, final Runnable r) {
        if (DumbService.isDumbAware(r)) {
            r.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(r, project));
        }
    }

    /**
     * 在 IntelliJ 事件队列中异步执行任务。
     *
     * @param r 待执行任务
     */
    public static void invokeLater(Runnable r) {
        ApplicationManager.getApplication().invokeLater(r);
    }

    /**
     * 按项目默认模态状态异步执行任务。
     *
     * @param p 当前项目
     * @param r 待执行任务
     */
    public static void invokeLater(Project p, Runnable r) {
        invokeLater(p, ModalityState.defaultModalityState(), r);
    }

    /**
     * 按指定模态状态异步执行任务。
     *
     * @param p 当前项目
     * @param state 模态状态
     * @param r 待执行任务
     */
    public static void invokeLater(final Project p, final ModalityState state, final Runnable r) {
        if (isNoBackgroundMode()) {
            r.run();
        } else {
            ApplicationManager.getApplication().invokeLater(DisposeAwareRunnable.create(r, p), state);
        }
    }

    /**
     * 生成 HTML 图片标签片段。
     *
     * @param url 图片地址
     * @return HTML 图片标签
     */
    public static String formatHtmlImage(URL url) {
        return "<img src=\"" + url + "\"> ";
    }

    /**
     * 在模块或项目范围内查找指定全限定名的 Java 类。
     *
     * @param qualifiedName 类全限定名
     * @param module 模块，可为空
     * @param project 当前项目
     * @return 匹配到的 PSI 类
     */
    public static PsiClass findPsiClass(final String qualifiedName, final Module module, final Project project) {
        final GlobalSearchScope scope = module == null ? GlobalSearchScope.projectScope(project) : GlobalSearchScope.moduleWithDependenciesScope(module);
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, scope);
    }

    /**
     * 获取 PSI 类所在的包。
     *
     * @param psiClass 目标类
     * @return 所在包，不存在时返回 {@code null}
     */
    public static PsiPackage getContainingPackage(@NotNull PsiClass psiClass) {
        PsiDirectory directory = psiClass.getContainingFile().getContainingDirectory();
        return directory == null ? null : JavaDirectoryService.getInstance().getPackage(directory);
    }

    /**
     * 在写操作上下文中执行任务。
     *
     * @param action 写操作逻辑
     */
    public static void runWriteAction(@NotNull Runnable action) {
        ApplicationManager.getApplication().runWriteAction(action);
    }

    /**
     * 去掉输入 URL 中冗余的主机、端口和查询参数，只保留匹配路径。
     *
     * @param pattern 用户输入的原始模式
     * @return 清洗后的路径模式
     */
    @NotNull
    public static String removeRedundancyMarkup(String pattern) {
        String localhostRegex = "(http(s?)://)?(localhost)(:\\d+)?";
        String hostAndPortRegex = "(http(s?)://)?" +
                "( " +
                "([a-zA-Z0-9]([a-zA-Z0-9\\\\-]{0,61}[a-zA-Z0-9])?\\\\.)+[a-zA-Z]{2,6} |" +
                "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)" +
                ")";

        String localhost = "localhost";
        if (pattern.contains(localhost)) {
            pattern = pattern.replaceFirst(localhostRegex, "");
        }
        // quick test if reg exp should be used
        if (pattern.contains("http:") || pattern.contains("https:")) {
            pattern = pattern.replaceFirst(hostAndPortRegex, "");
        }

        // TODO : resolve RequestMapping(params="method=someMethod")
        if (!pattern.contains("?")) {
            return pattern;
        }
        pattern = pattern.substring(0, pattern.indexOf("?"));
        return pattern;
    }
}
