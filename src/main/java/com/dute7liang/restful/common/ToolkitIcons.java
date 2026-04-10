package com.dute7liang.restful.common;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.dute7liang.restful.method.HttpMethod;

import javax.swing.*;

/**
 * 统一管理插件中使用到的图标资源。
 *
 * @author dute7liang
 */
public class ToolkitIcons {

    /**
     * HTTP 方法图标分组，负责将请求方法映射到对应的颜色图标。
     */
    public static class METHOD {
        /**
         * 根据 HTTP 方法返回对应的图标。
         *
         * @param method 请求方法
         * @return 对应的图标，未知方法时返回默认图标
         */
        public static Icon get(HttpMethod method) {
            if (method == null) {
                return UNDEFINED;
            }
            if (method.equals(HttpMethod.GET)) {
                return METHOD.GET;
            } else if (method.equals(HttpMethod.POST)) {
                return METHOD.POST;
            } else if (method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)) {
                return METHOD.PUT;
            } else if (method.equals(HttpMethod.DELETE)) {
                return METHOD.DELETE;
            }
            return null;
        }

        public static Icon GET = IconLoader.getIcon("/icons/method/g.png", ToolkitIcons.class);
        public static Icon PUT = IconLoader.getIcon("/icons/method/p2.png", ToolkitIcons.class);
        public static Icon POST = IconLoader.getIcon("/icons/method/p.png", ToolkitIcons.class);
        public static Icon PATCH = IconLoader.getIcon("/icons/method/p3.png", ToolkitIcons.class);
        public static Icon DELETE = IconLoader.getIcon("/icons/method/d.png", ToolkitIcons.class);
        public static Icon UNDEFINED = IconLoader.getIcon("/icons/method/undefined.png", ToolkitIcons.class);
    }

    public static final Icon MODULE = AllIcons.Nodes.ModuleGroup;
    public static final Icon Refresh = AllIcons.Actions.Refresh;
    public static final Icon SERVICE = IconLoader.getIcon("/icons/service.png", ToolkitIcons.class);
}
