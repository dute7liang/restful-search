package com.dute7liang.restful.method;

/**
 * 封装单条请求路径及其对应的 HTTP 方法。
 *
 * @author dute7liang
 */
public class RequestPath {
    String path;
    String method;

    /**
     * 创建一个请求路径对象。
     *
     * @param path 请求路径
     * @param method 请求方法
     */
    public RequestPath(String path, String method) {
        this.path = path;
        this.method = method;
    }

    /**
     * 返回当前请求路径。
     *
     * @return 请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 更新请求路径。
     *
     * @param path 新的请求路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 返回请求方法。
     *
     * @return 请求方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 更新请求方法。
     *
     * @param method 新的请求方法
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 将类级路径与方法级路径拼接成完整请求路径。
     *
     * @param classRequestPath 类级请求路径
     */
    public void concat(RequestPath classRequestPath) {
        String classUri = classRequestPath.getPath();
        String methodUri = this.path;
        // TODO
        if (!classUri.startsWith("/")) classUri = "/".concat(classUri);
        if (!classUri.endsWith("/")) classUri = classUri.concat("/");
        if (this.path.startsWith("/")) methodUri = this.path.substring(1, this.path.length());

        this.path = classUri.concat(methodUri);
    }
}
