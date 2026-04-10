package com.dute7liang.restful.method;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义插件中支持识别的 HTTP 方法枚举。
 *
 * @author dute7liang
 */
public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE, CONNECT;

    private static final Map<String, HttpMethod> methodMap = new HashMap(8);

    /**
     * 根据请求方法字符串解析出对应的枚举值。
     * 兼容类似 {@code RequestMethod.GET} 这样的输入形式。
     *
     * @param method 请求方法字符串
     * @return 对应的 HTTP 方法，无法识别时返回 {@code null}
     */
    public static HttpMethod getByRequestMethod(String method) {
        if (method == null || method.isEmpty()) {
            return null;
        }

        String[] split = method.split("\\.");

        if (split.length > 1) {
            method = split[split.length - 1].toUpperCase();
            return HttpMethod.valueOf(method);
        }

        return HttpMethod.valueOf(method.toUpperCase());
    }

    static {
        for (HttpMethod httpMethod : values()) {
            methodMap.put(httpMethod.name(), httpMethod);
        }
    }
}
