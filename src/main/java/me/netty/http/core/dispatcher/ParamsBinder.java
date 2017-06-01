package me.netty.http.core.dispatcher;

import me.netty.http.annnotation.RequestParams;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * @author tianwenjian
 * @create 2017-06-01 15:20
 **/
public class ParamsBinder {

    public static final Log logger = LogFactory.getLog(ParamsBinder.class);

        /**
     * 参数绑定
     * 这里暂时只实现request 和 response ，简单数据的绑定
     * 自定义一个request 和 response
     * @param parameters
     * @param request
     * @return
     */
    public static Object[] bindParams(Parameter[] parameters, MainProcessor processor){
        Object[] params = new Object[parameters.length];

        for(int i = 0; i < parameters.length; i++){
            Class clazz = parameters[i].getType();

            //绑定系统赋予的对象，如request,request 和 封装的参数
            if (clazz.equals(ServerHttpRequest.class)){
                params[i] = processor.getRequest();
            }else if(clazz.equals(ServerHttpResponse.class)){
                params[i] = processor.getResponse();
            } else if(parameters[i].getAnnotation(RequestParams.class) != null){
                params[i] = bindObject(clazz, processor.getRequest(), parameters[i].getAnnotation(RequestParams.class).isValidate());
            } else{  //绑定其他
                try {
                    params[i] = bindSingle(parameters[i].getName(), clazz, processor.getRequest());
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
    private static Object bindSingle(String name, Class clazz, ServerHttpRequest request) throws IllegalAccessException, InstantiationException {

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
    private static Object bindObject(Class clazz, ServerHttpRequest request, boolean check){
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
        } catch (Exception e){
            logger.error(e);
        }
        return null;
    }
}
