package com.dute7liang.restful.common;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtClassOrObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

// 处理 实体自关联，第二层自关联字段
public class KtClassHelper {
    KtClass psiClass;

    private static int autoCorrelationCount = 0; //标记实体递归
    private int listIterateCount = 0; //标记List递归
    private Module myModule;

    protected KtClassHelper(@NotNull KtClass psiClass) {
        this.psiClass = psiClass;
    }

    @NotNull
    protected Project getProject() {
        return psiClass.getProject();
    }

    @Nullable
    public KtClassOrObject findOnePsiClassByClassName(String className, Project project) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        KtClassOrObject kotlinClass = toKotlinClass(psiClass);
        if (kotlinClass != null) {
            return kotlinClass;
        }

        String shortClassName = className;
        int lastDot = className.lastIndexOf(".");
        if (lastDot >= 0 && lastDot < className.length() - 1) {
            shortClassName = className.substring(lastDot + 1);
        }

        Collection<KtClassOrObject> detectedClasses = tryDetectPsiClassByShortClassName(project, shortClassName);
        for (KtClassOrObject detectedClass : detectedClasses) {
            if (detectedClass.getFqName() != null && className.equals(detectedClass.getFqName().asString())) {
                return detectedClass;
            }
        }

        return detectedClasses.isEmpty() ? null : detectedClasses.iterator().next();
    }

    // PsiShortNamesCache : PsiClass:Demo    KtLightClassImpl:data class Greeting(val id: Long, val content: String) { 代码体 }
    public Collection<KtClassOrObject> tryDetectPsiClassByShortClassName(Project project, String shortClassName) {
        PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(shortClassName, GlobalSearchScope.allScope(project));
        Collection<KtClassOrObject> ktClassOrObjects = new ArrayList<>();
        Set<String> visitedKeys = new LinkedHashSet<>();
        for (PsiClass psiClass : psiClasses) {
            KtClassOrObject kotlinClass = toKotlinClass(psiClass);
            if (kotlinClass == null) {
                continue;
            }

            String key = kotlinClass.getFqName() != null
                    ? kotlinClass.getFqName().asString()
                    : kotlinClass.getContainingFile().getVirtualFile() + ":" + kotlinClass.getTextOffset();
            if (visitedKeys.add(key)) {
                ktClassOrObjects.add(kotlinClass);
            }
        }

        return ktClassOrObjects;
    }

    @Nullable
    private KtClassOrObject toKotlinClass(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        }

        PsiElement navigationElement = psiClass.getNavigationElement();
        if (navigationElement instanceof KtClassOrObject) {
            return (KtClassOrObject) navigationElement;
        }

        PsiElement originalNavigationElement = psiClass.getOriginalElement().getNavigationElement();
        if (originalNavigationElement instanceof KtClassOrObject) {
            return (KtClassOrObject) originalNavigationElement;
        }

        return null;
    }

    public static KtClassHelper create(@NotNull KtClass psiClass) {
        return new KtClassHelper(psiClass);
    }
}
