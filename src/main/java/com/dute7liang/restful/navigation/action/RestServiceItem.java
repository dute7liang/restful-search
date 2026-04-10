package com.dute7liang.restful.navigation.action;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.dute7liang.restful.common.ToolkitIcons;
import com.dute7liang.restful.method.HttpMethod;
import com.dute7liang.restful.method.action.ModuleHelper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;

/**
 * 表示一个可在“Go to Service”弹窗中展示和跳转的 REST 服务项。
 *
 * @author dute7liang
 */
public class RestServiceItem implements NavigationItem {
    private PsiMethod psiMethod; // Java 方法元素
    private PsiElement psiElement; // 用于导航的 PSI 元素
    private Module module;
    private HttpMethod method; // 请求方法
    private String url; // 请求映射路径
    private final String locationText;
    private final Project project;
    private final VirtualFile virtualFile;
    private final int navigationOffset;

    /**
     * 使用导航元素、请求方法和路径创建一个服务项。
     *
     * @param psiElement 导航目标元素
     * @param requestMethod 请求方法字符串
     * @param urlPath 请求路径
     */
    public RestServiceItem(PsiElement psiElement, String requestMethod, String urlPath) {
        this.psiElement = psiElement;
        this.project = psiElement.getProject();
        this.virtualFile = psiElement.getContainingFile() == null ? null : psiElement.getContainingFile().getVirtualFile();
        this.navigationOffset = psiElement.getTextOffset();
        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
        }
        if (requestMethod != null) {
            method = HttpMethod.getByRequestMethod(requestMethod);
        }

        this.url = urlPath;
        this.locationText = buildLocationText(psiElement);
    }

    /**
     * 返回服务项名称，当前直接使用 URL 路径作为名称。
     *
     * @return 服务名称
     */
    @Nullable
    @Override
    public String getName() {
        return url;
    }

    /**
     * 返回服务项展示对象。
     *
     * @return 展示对象
     */
    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return new RestServiceItemPresentation();
    }

    /**
     * 跳转到当前服务项对应的源码位置。
     *
     * @param requestFocus 是否请求焦点
     */
    @Override
    public void navigate(boolean requestFocus) {
        if (project != null && virtualFile != null) {
            new OpenFileDescriptor(project, virtualFile, navigationOffset).navigate(requestFocus);
        }
    }

    /**
     * 判断当前服务项是否可以导航。
     *
     * @return 可导航时返回 {@code true}
     */
    @Override
    public boolean canNavigate() {
        return project != null && virtualFile != null && virtualFile.isValid();
    }

    /**
     * 判断当前服务项是否支持导航到源码。
     *
     * @return 固定返回 {@code true}
     */
    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    /**
     * 判断服务项是否匹配当前查询文本。
     *
     * @param queryText 用户输入文本
     * @return 匹配时返回 {@code true}
     */
    public boolean matches(String queryText) {
        String pattern = queryText;
        if (pattern.equals("/")) {
            return true;
        }

        com.intellij.psi.codeStyle.MinusculeMatcher matcher = com.intellij.psi.codeStyle.NameUtil.buildMatcher("*" + pattern, com.intellij.psi.codeStyle.NameUtil.MatchingCaseSensitivity.NONE);
        return matcher.matches(this.url);
    }

    /**
     * 构建结果列表中显示的位置文本，例如 `UserController#list`。
     *
     * @param psiElement 当前导航元素
     * @return 位置说明文本
     */
    @Nullable
    private String buildLocationText(PsiElement psiElement) {
        String location = null;

        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (psiMethod.getContainingClass() != null && psiMethod.getContainingClass().getName() != null) {
                location = psiMethod.getContainingClass().getName().concat("#").concat(psiMethod.getName());
            }
        } else if (psiElement instanceof KtNamedFunction) {
            KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
            PsiElement parent = psiElement.getParent();
            PsiElement grandParent = parent == null ? null : parent.getParent();
            if (grandParent instanceof KtClass && ((KtClass) grandParent).getName() != null) {
                location = ((KtClass) grandParent).getName().concat("#").concat(ktNamedFunction.getName());
            }
        }

        return location == null ? null : "(" + location + ")";
    }

    /**
     * 结果列表中的展示对象实现。
     */
    private class RestServiceItemPresentation implements ItemPresentation {
        /**
         * 返回显示在列表主标题中的文本。
         *
         * @return 服务路径
         */
        @Nullable
        @Override
        public String getPresentableText() {
            return url;
        }

        /**
         * 返回显示在列表副标题中的位置信息。
         *
         * @return 位置文本
         */
        @Nullable
        @Override
        public String getLocationString() {
            return locationText;
        }

        /**
         * 返回服务项图标。
         *
         * @param unused 平台保留参数
         * @return HTTP 方法对应图标
         */
        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
            return ToolkitIcons.METHOD.get(method);
        }
    }

    /**
     * 返回当前服务项所属模块。
     *
     * @return 所属模块
     */
    public Module getModule() {
        return module;
    }

    /**
     * 返回绑定的 Java 方法元素。
     *
     * @return Java 方法 PSI
     */
    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    /**
     * 设置绑定的 Java 方法元素。
     *
     * @param psiMethod Java 方法 PSI
     */
    public void setPsiMethod(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    /**
     * 返回当前服务项的 HTTP 方法。
     *
     * @return HTTP 方法
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * 更新当前服务项的 HTTP 方法。
     *
     * @param method HTTP 方法
     */
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    /**
     * 返回当前服务项路径。
     *
     * @return 请求路径
     */
    public String getUrl() {
        return url;
    }

    /**
     * 更新当前服务项路径。
     *
     * @param url 请求路径
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 返回带模块主机前缀的完整 URL。
     *
     * @return 完整 URL
     */
    public String getFullUrl() {
        if (module == null) {
            return getUrl();
        }

        ModuleHelper moduleHelper = ModuleHelper.create(module);
        return moduleHelper.getServiceHostPrefix() + getUrl();
    }

    /**
     * 设置当前服务项所属模块。
     *
     * @param module 所属模块
     */
    public void setModule(Module module) {
        this.module = module;
    }

    /**
     * 返回当前绑定的导航元素。
     *
     * @return PSI 导航元素
     */
    public PsiElement getPsiElement() {
        return psiElement;
    }
}
