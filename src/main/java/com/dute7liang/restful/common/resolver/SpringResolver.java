package com.dute7liang.restful.common.resolver;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.dute7liang.restful.annotations.PathMappingAnnotation;
import com.dute7liang.restful.annotations.SpringControllerAnnotation;
import com.dute7liang.restful.annotations.SpringRequestMethodAnnotation;
import com.dute7liang.restful.common.spring.RequestMappingAnnotationHelper;
import com.dute7liang.restful.method.RequestPath;
import com.dute7liang.restful.method.action.PropertiesHandler;
import com.dute7liang.restful.navigation.action.RestServiceItem;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtValueArgument;
import org.jetbrains.kotlin.psi.KtValueArgumentList;
import org.jetbrains.kotlin.psi.KtValueArgumentName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Spring 路由解析器，负责扫描 Java 和 Kotlin 控制器并组装 REST 服务项。
 *
 * @author dute7liang
 */
public class SpringResolver extends BaseServiceResolver {
    PropertiesHandler propertiesHandler;

    /**
     * 创建模块级 Spring 解析器。
     *
     * @param module 当前模块
     */
    public SpringResolver(Module module) {
        myModule = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    /**
     * 创建项目级 Spring 解析器。
     *
     * @param project 当前项目
     */
    public SpringResolver(Project project) {
        myProject = project;
    }

    /**
     * 在指定作用域内扫描 Spring 控制器并构建服务项列表。
     *
     * @param project 当前项目
     * @param globalSearchScope 当前搜索范围
     * @return 服务项列表
     */
    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();

        // TODO: 这种实现的局限了其他方式实现的url映射（xml（类似struts），webflux routers）
        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        for (PathMappingAnnotation controllerAnnotation : supportedAnnotations) {
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(controllerAnnotation.getShortName(), project, globalSearchScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();

                PsiClass psiClass = (PsiClass) psiElement;
                List<RestServiceItem> serviceItemList = getServiceItemList(psiClass);
                itemList.addAll(serviceItemList);
            }

            Collection<KtClass> kotlinControllerClasses = findKotlinControllerClasses(project, globalSearchScope, controllerAnnotation);
            for (KtClass ktClass : kotlinControllerClasses) {
                itemList.addAll(getServiceItemList(ktClass));
            }
        }

        return itemList;
    }

    /**
     * 解析 Java 控制器类中的全部请求方法。
     *
     * @param psiClass Java 控制器类
     * @return 服务项列表
     */
    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {
        PsiMethod[] psiMethods = psiClass.getMethods();
        if (psiMethods == null) {
            return new ArrayList<>();
        }

        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiMethod);

            for (RequestPath classRequestPath : classRequestPaths) {
                for (RequestPath methodRequestPath : methodRequestPaths) {
                    String path = classRequestPath.getPath();

                    PsiElement navigationElement = toNavigationElement(psiMethod);
                    RestServiceItem item = createRestServiceItem(navigationElement, path, methodRequestPath);
                    item.setPsiMethod(psiMethod);
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    /**
     * 解析 Kotlin 控制器类中的全部请求方法。
     *
     * @param ktClass Kotlin 控制器类
     * @return 服务项列表
     */
    protected List<RestServiceItem> getServiceItemList(KtClass ktClass) {
        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = getRequestPaths(ktClass);
        List<KtNamedFunction> ktNamedFunctions = getKtNamedFunctions(ktClass);

        for (KtNamedFunction function : ktNamedFunctions) {
            List<RequestPath> methodRequestPaths = getRequestPaths(function);
            for (RequestPath classRequestPath : classRequestPaths) {
                for (RequestPath methodRequestPath : methodRequestPaths) {
                    RequestPath requestPath = new RequestPath(methodRequestPath.getPath(), methodRequestPath.getMethod());
                    requestPath.concat(classRequestPath);
                    itemList.add(createRestServiceItem(function, "", requestPath));
                }
            }
        }

        return itemList;
    }

    /**
     * 在搜索范围内找出所有带有目标注解的 Kotlin 控制器类。
     *
     * @param project 当前项目
     * @param globalSearchScope 当前搜索范围
     * @param controllerAnnotation 控制器注解定义
     * @return Kotlin 控制器集合
     */
    private Collection<KtClass> findKotlinControllerClasses(Project project, GlobalSearchScope globalSearchScope, PathMappingAnnotation controllerAnnotation) {
        List<KtClass> controllerClasses = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);

        for (com.intellij.openapi.vfs.VirtualFile file : FilenameIndex.getAllFilesByExt(project, "kt", globalSearchScope)) {
            PsiElement psiFile = psiManager.findFile(file);
            if (!(psiFile instanceof KtFile)) {
                continue;
            }
            collectControllerClasses(((KtFile) psiFile).getDeclarations(), controllerAnnotation, controllerClasses);
        }

        return controllerClasses;
    }

    /**
     * 递归收集 Kotlin 声明中的控制器类。
     *
     * @param declarations 当前声明列表
     * @param controllerAnnotation 控制器注解定义
     * @param controllerClasses 结果集合
     */
    private void collectControllerClasses(List<KtDeclaration> declarations, PathMappingAnnotation controllerAnnotation, List<KtClass> controllerClasses) {
        for (KtDeclaration declaration : declarations) {
            if (!(declaration instanceof KtClass)) {
                continue;
            }

            KtClass ktClass = (KtClass) declaration;
            if (hasControllerAnnotation(ktClass, controllerAnnotation)) {
                controllerClasses.add(ktClass);
            }

            collectControllerClasses(ktClass.getDeclarations(), controllerAnnotation, controllerClasses);
        }
    }

    /**
     * 判断 Kotlin 类上是否声明了指定的控制器注解。
     *
     * @param ktClass Kotlin 类
     * @param controllerAnnotation 控制器注解定义
     * @return 命中时返回 {@code true}
     */
    private boolean hasControllerAnnotation(KtClass ktClass, PathMappingAnnotation controllerAnnotation) {
        if (ktClass.getModifierList() == null) {
            return false;
        }

        for (KtAnnotationEntry annotationEntry : ktClass.getModifierList().getAnnotationEntries()) {
            if (annotationEntry.getCalleeExpression() == null) {
                continue;
            }

            String annotationName = annotationEntry.getCalleeExpression().getText();
            if (controllerAnnotation.getShortName().equals(annotationName) || controllerAnnotation.getQualifiedName().equals(annotationName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将 Java 方法导航目标尽量转换成 Kotlin 原始元素。
     *
     * @param psiMethod Java 方法
     * @return 更适合导航的 PSI 元素
     */
    private PsiElement toNavigationElement(PsiMethod psiMethod) {
        PsiElement navigationElement = psiMethod.getNavigationElement();
        if (navigationElement instanceof KtNamedFunction) {
            return navigationElement;
        }
        return psiMethod;
    }

    /**
     * 收集 Kotlin 类中声明的函数列表。
     *
     * @param ktClass Kotlin 类
     * @return 函数列表
     */
    private List<KtNamedFunction> getKtNamedFunctions(KtClass ktClass) {
        List<KtNamedFunction> ktNamedFunctions = new ArrayList<>();
        List<KtDeclaration> declarations = ktClass.getDeclarations();

        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtNamedFunction) {
                KtNamedFunction fun = (KtNamedFunction) declaration;
                ktNamedFunctions.add(fun);
            }
        }
        return ktNamedFunctions;
    }

    /**
     * 解析 Kotlin 类上的请求路径。
     *
     * @param ktClass Kotlin 类
     * @return 请求路径列表
     */
    private List<RequestPath> getRequestPaths(KtClass ktClass) {
        String defaultPath = "/";
        if (ktClass.getModifierList() == null) {
            return Collections.singletonList(new RequestPath(defaultPath, null));
        }
        List<KtAnnotationEntry> annotationEntries = ktClass.getModifierList().getAnnotationEntries();

        List<RequestPath> requestPaths = getRequestMappings(defaultPath, annotationEntries);
        return requestPaths;
    }

    /**
     * 解析 Kotlin 方法上的请求路径。
     *
     * @param fun Kotlin 方法
     * @return 请求路径列表
     */
    private List<RequestPath> getRequestPaths(KtNamedFunction fun) {
        String defaultPath = "/";
        if (fun.getModifierList() == null) {
            return Collections.singletonList(new RequestPath(defaultPath, null));
        }
        List<KtAnnotationEntry> annotationEntries = fun.getModifierList().getAnnotationEntries();
        List<RequestPath> requestPaths = getRequestMappings(defaultPath, annotationEntries);
        return requestPaths;
    }

    /**
     * 批量解析 Kotlin 注解列表中的请求路径。
     *
     * @param defaultPath 默认路径
     * @param annotationEntries 注解列表
     * @return 请求路径列表
     */
    private List<RequestPath> getRequestMappings(String defaultPath, List<KtAnnotationEntry> annotationEntries) {
        List<RequestPath> requestPaths = new ArrayList<>();
        for (KtAnnotationEntry entry : annotationEntries) {
            List<RequestPath> requestMappings = getRequestMappings(defaultPath, entry);
            requestPaths.addAll(requestMappings);
        }
        return requestPaths;
    }

    /**
     * 解析单个 Kotlin Spring 注解中的方法和路径配置。
     *
     * @param defaultPath 默认路径
     * @param entry 注解条目
     * @return 请求路径列表
     */
    private List<RequestPath> getRequestMappings(String defaultPath, KtAnnotationEntry entry) {
        List<RequestPath> requestPaths = new ArrayList<>();
        List<String> methodList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();

        String annotationName = entry.getCalleeExpression().getText();
        SpringRequestMethodAnnotation requestMethodAnnotation = SpringRequestMethodAnnotation.getByShortName(annotationName);
        if (requestMethodAnnotation == null) {
            return new ArrayList<>();
        }

        if (requestMethodAnnotation.methodName() != null) {
            methodList.add(requestMethodAnnotation.methodName());
        } else {
            methodList.addAll(getAttributeValues(entry, "method"));
        }

        if (entry.getValueArgumentList() != null) {
            List<String> mappingValues = getAttributeValues(entry, null);
            if (!mappingValues.isEmpty()) {
                pathList.addAll(mappingValues);
            } else {
                pathList.addAll(getAttributeValues(entry, "value"));
            }

            pathList.addAll(getAttributeValues(entry, "path"));
        }

        if (pathList.isEmpty()) pathList.add(defaultPath);

        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    requestPaths.add(new RequestPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                requestPaths.add(new RequestPath(path, null));
            }
        }

        return requestPaths;
    }

    /**
     * 从 Kotlin 注解参数中读取指定属性值。
     *
     * @param entry 注解条目
     * @param attribute 属性名，读取默认值时传 {@code null}
     * @return 属性值列表
     */
    private List<String> getAttributeValues(KtAnnotationEntry entry, String attribute) {
        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();

        if (valueArgumentList == null) return Collections.emptyList();

        List<KtValueArgument> arguments = valueArgumentList.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            KtValueArgument ktValueArgument = arguments.get(i);
            KtValueArgumentName argumentName = ktValueArgument.getArgumentName();

            KtExpression argumentExpression = ktValueArgument.getArgumentExpression();

            if ((argumentName == null && attribute == null) || (argumentName != null && argumentName.getText().equals(attribute))) {
                List<String> methodList = new ArrayList<>();
                if (argumentExpression.getText().startsWith("arrayOf")) {
                    List<KtValueArgument> pathValueArguments = ((KtCallExpression) argumentExpression).getValueArguments();
                    for (KtValueArgument pathValueArgument : pathValueArguments) {
                        methodList.add(pathValueArgument.getText().replace("\"", ""));
                    }
                } else if (argumentExpression.getText().startsWith("[")) {
                    List<KtExpression> innerExpressions = ((KtCollectionLiteralExpression) argumentExpression).getInnerExpressions();
                    for (KtExpression ktExpression : innerExpressions) {
                        methodList.add(ktExpression.getText().replace("\"", ""));
                    }
                } else {
                    PsiElement[] paths = ktValueArgument.getArgumentExpression().getChildren();
                    methodList.add(paths.length == 0 ? "" : paths[0].getText());
                }

                return methodList;
            }
        }

        return new ArrayList<>();
    }
}
