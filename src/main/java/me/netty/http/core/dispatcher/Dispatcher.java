package me.netty.http.core.dispatcher;

import io.netty.buffer.ByteBuf;
import me.netty.http.annnotation.*;
import me.netty.http.core.ServerContext;
import me.netty.http.core.BullInterceptor;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.asyn.ProcessRunnable;
import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import me.netty.http.utils.MyClassUtils;
import io.netty.buffer.ByteBufUtil;
import me.netty.http.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private ExecutorService executorService;  //线程池

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

                    //注入spring的Bean
                    this.initFieldFromSpringBean(o);
                    //处理保存接口的定义
                    this.bindMethod(methods, o);
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
     * 处理Controller定义的接口
     * @param methods
     * @param target
     */
    private void bindMethod(Method[] methods, Object target){
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

            //判断是否 return view
            boolean isReturnView = false;
            ReturnModelAndView retunModelAndView = method.getAnnotation(ReturnModelAndView.class);
            if (retunModelAndView != null){
                isReturnView = true;
            }

            //保存
            Function function = new Function(target, mapping.method(), method, mapping.isAsyn(), isReturnView);
            functionMap.put(value, function);
            logger.debug("add function : " + value);
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
    public boolean doService(ServerHttpRequest request, ServerHttpResponse response, MainProcessor mainProcessor){

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
        Object res;
        try {
            res = function.doMethod(mainProcessor);

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
        } catch (Exception e) {
            logger.error("处理失败！", e);
            MainProcessor.productSimpleResponse(mainProcessor.getResponse(),mainProcessor.getRequest(),INTERNAL_SERVER_ERROR, "执行方法时发生了错误！");
            mainProcessor.sendResponse();
            return;
        }
    }

    /**
     * 注入Spring的Bean
     * @param target
     */
    public void initFieldFromSpringBean(Object target){
        if (serverContext.getSpringContext() == null){
            return;
        }

        Field[] fields = target.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0){
            return;
        }

        for (Field field : fields){
            Object bean = null;

            SpringBean beanAnnotation = field.getAnnotation(SpringBean.class);
            if (beanAnnotation == null){
                if (logger.isDebugEnabled()) {
                    logger.debug("ignore no annotation property:" + field.getName());
                }
                continue;
            }

            if (StringUtils.isNotEmpty(beanAnnotation.value())){
                bean = serverContext.getSpringContext().getBean(beanAnnotation.value());
            }else{
                bean = serverContext.getSpringContext().getBean(field.getType());
            }
            try {
                ReflectionUtils.setValueByFieldName(target, field.getName(), bean);
                if (logger.isDebugEnabled()) {
                    logger.debug("注入bean：" + bean.getClass().getName() + "->>" + target.getClass().getName());
                }
            } catch (NoSuchFieldException  | IllegalAccessException e) {
                logger.error("注入spring的Bean失败", e);
                //throw new RuntimeException("注入spring的Bean失败");
            }

        }
    }
}
