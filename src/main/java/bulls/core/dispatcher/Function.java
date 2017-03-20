package bulls.core.dispatcher;

import java.lang.reflect.Method;

/**
 * 开发接口的描述
 * Created by 1 on 2017/2/26.
 */
public class Function {
    private Object controllerObject; //
    private String requestMethod; //http method
    private Method method; //method well be do

    public Function(Object obj, String requestMethod, Method method){
        this.controllerObject = obj;
        this.requestMethod = requestMethod;
        this.method = method;
    }

    public Object getControllerObject() {
        return controllerObject;
    }

    public void setControllerObject(Object controllerObject) {
        this.controllerObject = controllerObject;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
