package me.netty.http.core.dispatcher;

import io.netty.buffer.ByteBuf;
import me.netty.http.ServerContext;
import me.netty.http.annnotation.Controller;
import me.netty.http.annnotation.Interceptor;
import me.netty.http.annnotation.Mapping;
import me.netty.http.annnotation.RequestParams;
import me.netty.http.core.BullInterceptor;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.asyn.ProcessRunnable;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.http.BullsHttpResponse;
import me.netty.http.utils.MyClassUtils;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * 核心转发类，把请求路由到对应的方法上，并且绑定对象
 * 反射时Java 已经实现了缓存， 不再另外实现缓存
 *
 * Created by 1 on 2017/2/25.
 */
public class Dispatcher {

    private static Log logger = LogFactory.getLog(Dispatcher.class);

    private Map<String, Function> functionMap;
    private ServerContext serverContext;
    private ExecutorService executorService;

    public Dispatcher(ServerContext serverContext){
        this.functionMap = new ConcurrentHashMap<>(128);
        this.serverContext = serverContext;

        //异步的处理，当线程和队列都满了时，拒绝请求,
        // 因为设计的初衷是为了应对可能阻塞的服务，
        // 所以maxPoolSize设置的大一点，而为了节约内存，队列适当小一点，为了work线程不出现饥饿，使用公平锁
        this.executorService = new ThreadPoolExecutor(5,
                500,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100, true),
                (r, executor) -> {
                    ProcessRunnable runnable = (ProcessRunnable) r;
                    MainProcessor processor = runnable.getMainProcessor();
                    MainProcessor.productSimpleResponse(processor.getResponse(),processor.getRequest(),INTERNAL_SERVER_ERROR,"服务器不堪重负，请待会再试");
                    processor.sendResponse();
                });
    }

    /**
     * 初始化包扫描的类
     * @param packages
     */
    public void addPackages(List<String> packages) throws Exception{
        for(String s : packages){
            List<Class<?>> list =  MyClassUtils.getClasses(s);
            //logger.debug("扫描包:" + s + "获得class:" + list.size());
            for(Class c: list){
                //寻找Controller
                Controller controller = (Controller) c.getAnnotation(Controller.class);
                if (controller != null){
                    Method[] methods = c.getDeclaredMethods();

                    Object o = c.newInstance();
                    for (Method method : methods){
                        Mapping mapping = method.getAnnotation(Mapping.class);
                        if (mapping == null){
                            continue;
                        }

                        String value = mapping.value();
                        if (functionMap.get(value) != null){
                            logger.error("重复的请求路径，请检查");
                            throw new RuntimeException("重复的请求路径，请检查");
                        }

                        //在value前加上/
                        if (!value.startsWith("/")){
                            value = "/" + value;
                        }

                        //保存
                        Function function = new Function(o, mapping.method(), method, mapping.isAsyn());
                        functionMap.put(value, function);
                        logger.debug("add function : " + value);
                    }
                }

                //寻找拦截器
               Interceptor interceptor = (Interceptor) c.getAnnotation(Interceptor.class);
                if (interceptor != null){
                    logger.debug("add interceptor" + interceptor.toString());
                    this.serverContext.addInterceptor((BullInterceptor) c.newInstance());
                }
            }
        }
    }

    /**
     * 获取到function
     * @param path
     * @return
     */
    public Function getFunction(String path){
        return functionMap.get(path);
    }

    /**
     * 处理系统定义的资源
     * @param response
     * @param request
     * @return boolean 是否处理
     */
    public boolean doService(BullsHttpRequest request, BullsHttpResponse response, MainProcessor mainProcessor){

        String path = request.getRequestPath();

        //检查有无此方法
        Dispatcher dispatcher = ServerContext.getServerContext(request).getDispatcher();
        if (dispatcher == null){
            logger.info("dispatcher 没有初始化");
            return false;
        }
        Function function = dispatcher.getFunction(path);
        if (function == null){
            return false;
        }

        //检查Method是否匹配
        if (function.getHttpMethod() != null && !function.getHttpMethod().equals("") && !request.method().asciiName().equals(function.getHttpMethod())){
            logger.debug("请求method 不配陪" + path);
            MainProcessor.productSimpleResponse(response,request,BAD_REQUEST, "请求方法不匹配！");
            mainProcessor.sendResponse();
            return true;
        }

        //如果异步，线程池
        if(function.isAsyn()) {
            ByteBuf byteBuf = mainProcessor.getRequest().content();
            if (byteBuf != null){
                byteBuf.retain();
            }

            ProcessRunnable runnable = new ProcessRunnable(mainProcessor, this, function);
            this.executorService.submit(runnable);
        }else {
            this.doMethod(mainProcessor, function);
        }
        //表示已经接受了这个处理
        return true;
    }

    /**
     * 处理的方法
     * @param mainProcessor
     * @param function
     */
    public void doMethod(MainProcessor mainProcessor, Function function){
        //处理
        Method method = function.getMethod();
        Object res;
        try {
            Parameter[] parameters = method.getParameters();
            if (parameters == null || parameters.length == 0){
                res = method.invoke(function.getControllerObject());
            }else{
                //绑定参数
                Object[] params = this.bindParams(parameters, mainProcessor.getRequest(), mainProcessor.getResponse());
                res = method.invoke(function.getControllerObject(), params);
            }

            //构造返回响应对象
            if(res instanceof String){
                ByteBufUtil.writeUtf8(mainProcessor.getResponse().content(), (String)res);
            }else{
                //序列化？
                // todo 这里加上序列化， 另外考虑传入content type
            }
            mainProcessor.getResponse().headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            mainProcessor.getResponse().headers().setInt(CONTENT_LENGTH, mainProcessor.getResponse().content().readableBytes());
            mainProcessor.sendResponse();
            return;
        } catch (IllegalAccessException e) {
            logger.error("处理失败！", e);
            MainProcessor.productSimpleResponse(mainProcessor.getResponse(),mainProcessor.getRequest(),INTERNAL_SERVER_ERROR, "执行方法时发生了错误！");
            mainProcessor.sendResponse();
            return;
        } catch (InvocationTargetException e) {
            logger.error("处理失败！", e);
            MainProcessor.productSimpleResponse(mainProcessor.getResponse(),mainProcessor.getRequest(),INTERNAL_SERVER_ERROR, "执行方法时发生了错误！");
            mainProcessor.sendResponse();
            return;
        }
    }

    /**
     * 参数绑定
     * 这里暂时只实现request 和 response ，简单数据的绑定
     * 自定义一个request 和 response
     * @param parameters
     * @param request
     * @return
     */
    private  Object[] bindParams(Parameter[] parameters, BullsHttpRequest request, BullsHttpResponse response){
        Object[] params = new Object[parameters.length];

        for(int i = 0; i < parameters.length; i++){
            Class clazz = parameters[i].getType();

            //绑定系统赋予的对象，如request,request 和 封装的参数
            if (clazz.equals(BullsHttpRequest.class)){
                params[i] = request;
            }else if(clazz.equals(BullsHttpResponse.class)){
                params[i] = response;
            } else if(parameters[i].getAnnotation(RequestParams.class) != null){
                params[i] = this.bindObject(clazz, request, parameters[i].getAnnotation(RequestParams.class).isValidate());
            } else{  //绑定其他
                try {
                    params[i] = this.bind(parameters[i].getName(), clazz, request);
                } catch (InstantiationException e) {
                    logger.error("绑定参数失败", e);
                } catch (IllegalAccessException e) {
                    logger.error("绑定参数失败", e);
                }
            }
        }
        return params;
    }

    /**
     * 绑定参数中获取的对象
     * 目前只支持了 String Integer,Double 可以考虑类似spring MVC 的数据绑定模式
     * @param name
     * @param clazz
     * @param request
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object bind(String name, Class clazz, BullsHttpRequest request) throws IllegalAccessException, InstantiationException {

        String value = request.getPram(name);
        if (value == null || value.equals("")){
            return clazz.newInstance();
        }

        if (clazz.equals(String.class)){
            return value;
        }
        if (clazz.equals(Integer.class)){
            return Integer.valueOf(value);
        }
        if (clazz.equals(Double.class)){
            return Double.valueOf(value);
        }
        return clazz.newInstance();
    }

    /**
     * 对象参数封装，暂时使用的BeanUtils，需要时修改, 数据校验还没有实现
     * @param clazz
     * @param request
     * @param check
     * @return
     */
    private Object bindObject(Class clazz, BullsHttpRequest request, boolean check){
        try {
            Object object = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields){
                String name = field.getName();
                String value = request.getPram(name);
                if (value != null){
                    BeanUtils.setProperty(object, name, value);
                }
                if (check){
                    //do check
                }
            }
            return object;
        } catch (InstantiationException e) {
            logger.error(e);
        } catch (IllegalAccessException e) {
            logger.error(e);
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }
}
