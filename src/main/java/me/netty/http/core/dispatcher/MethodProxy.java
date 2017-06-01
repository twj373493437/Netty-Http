package me.netty.http.core.dispatcher;

/**
 * 任务执行器<br>
 * 动态生成代理对象，继承这个接口<br>
 *     提高性能
 * @author tianwenjian
 * @create 2017-06-01 14:24
 **/
public interface MethodProxy {

    /**
     * 执行的方法
     * @param processor 处理的context
     * @param object
     * @return
     */
    Object doMethod(Object[] params, Object object);
}
