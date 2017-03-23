package me.netty.http.core.dispatcher;

import java.lang.reflect.Method;

/**
 * 开发接口的描述
 * Created by 1 on 2017/2/26.
 */
public class Function {
    private Object controllerObject; //
    private String httpMethod; //http method
    private Method method; //method well be do
    private boolean isAsyn; //是否异步

    public Function(Object obj, String httpMethod, Method method){
        this.controllerObject = obj;
        this.httpMethod = httpMethod;
        this.method = method;
        isAsyn = false;
    }

    public Function(Object obj, String httpMethod, Method method, boolean isAsyn){
        this(obj, httpMethod, method);
        this.isAsyn = isAsyn;
    }

    public Object getControllerObject() {
        return controllerObject;
    }

    public void setControllerObject(Object controllerObject) {
        this.controllerObject = controllerObject;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public boolean isAsyn() {
        return isAsyn;
    }
}
