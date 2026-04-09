package com.dute7liang.restful.navigation.action;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.openapi.vfs.VirtualFile;
import com.dute7liang.restful.common.ToolkitIcons;
import com.dute7liang.restful.method.HttpMethod;
import com.dute7liang.restful.method.action.ModuleHelper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import javax.swing.*;

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

    @Nullable
    @Override
    public String getName() {
        return url;
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return new RestServiceItemPresentation();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (project != null && virtualFile != null) {
            new OpenFileDescriptor(project, virtualFile, navigationOffset).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return project != null && virtualFile != null && virtualFile.isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }
    public boolean matches(String queryText) {
        String pattern = queryText;
        if (pattern.equals("/")) {
            return true;
        }

        com.intellij.psi.codeStyle.MinusculeMatcher matcher = com.intellij.psi.codeStyle.NameUtil.buildMatcher("*" + pattern, com.intellij.psi.codeStyle.NameUtil.MatchingCaseSensitivity.NONE);
        return matcher.matches(this.url);
    }

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

    private class RestServiceItemPresentation implements ItemPresentation {
        @Nullable
        @Override
        public String getPresentableText() {
            return url;
        }

        @Nullable
        @Override
        public String getLocationString() {
            return locationText;
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
            return ToolkitIcons.METHOD.get(method);
        }
    }

    public Module getModule() {
        return module;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public void setPsiMethod(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        if (module == null) {
            return getUrl();
        }

        ModuleHelper moduleHelper = ModuleHelper.create(module);
        return moduleHelper.getServiceHostPrefix() + getUrl();
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }
}
