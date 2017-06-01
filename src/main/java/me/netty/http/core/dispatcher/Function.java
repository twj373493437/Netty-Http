package me.netty.http.core.dispatcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import me.netty.http.core.MainProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 开发接口的描述
 * Created by 1 on 2017/2/26.
 */
public class Function {

    public static final Log logger = LogFactory.getLog(Function.class);

    private static final String PROXY_CLASS_NAME_SUFFIX = "$proxy";
    private static int PROXY_CLASS_INDEX = 1;

    private String httpMethod; //http method
    private boolean isAsyn; //是否异步
    private MethodProxy proxy;
    private Object targetController;
    private Method method;

    public Function(Object obj, String httpMethod, Method method, boolean isAsyn) {
        this.httpMethod = httpMethod;
        isAsyn = false;
        this.isAsyn = isAsyn;
        this.targetController = obj;
        this.method = method;
        try {
            this.proxy = this.getMethodProxy(method, obj);
        } catch (Exception e) {
            logger.error("获取运行对象时发生错误", e);
        }
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public boolean isAsyn() {
        return isAsyn;
    }

    /**
     * 执行此方法
     *
     * @return
     */
    public Object doMethod(MainProcessor processor) {
        //得到绑定的参数
        Object[] params = ParamsBinder.bindParams(this.method.getParameters(), processor);
        return proxy.doMethod(params, targetController);
    }

    /**
     * 获取代理对象<br>
     * 使用动态写代码的方式
     *
     * @param method
     * @return
     */
    private MethodProxy getMethodProxy(Method method, Object controllerObject) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        String proxyClassName = MethodProxy.class.getName() + PROXY_CLASS_NAME_SUFFIX + PROXY_CLASS_INDEX;
        PROXY_CLASS_INDEX++;

        CtClass ctInterface = cp.getCtClass(MethodProxy.class.getName());
        CtClass proxyClass = cp.makeClass(proxyClassName);
        proxyClass.addInterface(ctInterface);

        //执行的方法
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("public Object doMethod( Object[] params, Object object){");

        //初始化参数（分开声明）
        Parameter[] parameters = method.getParameters();
        for (int j = 0; j < parameters.length; j++) {
            codeBuilder.append(parameters[j].getType().getName() +  " var" + j + " = params["+ j +"];");
        }

        //调用将要进入的业务层
        codeBuilder.append("Object result = ((" + controllerObject.getClass().getName() + ")object).");
        codeBuilder.append(method.getName());
        codeBuilder.append("(");

        //加入参数
        for (int j = 0; j < parameters.length; j++) {
            if (j != 0) {
                codeBuilder.append(", ");
            }
            codeBuilder.append("var" + j);
        }
        codeBuilder.append(");");
        codeBuilder.append("return result; }");

        if (logger.isDebugEnabled()) {
            logger.debug(codeBuilder.toString());
        }

        //添加方法到代理类
        CtMethod cm = CtNewMethod.make(codeBuilder.toString(), proxyClass);
        proxyClass.addMethod(cm);

        return (MethodProxy) proxyClass.toClass().newInstance();
    }
}
