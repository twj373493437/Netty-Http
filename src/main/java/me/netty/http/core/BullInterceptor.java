package me.netty.http.core;

import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;

/**
 * Created by 1 on 2017/3/13.
 */
public interface BullInterceptor {

    /**
     * @param request
     * 处理之前调用
     * @param response
     * @return
     */
    boolean beforeHandle(ServerHttpRequest request, ServerHttpResponse response);

    /**
     * 处理之后调用，哦也
     * @param request
     * @param response
     * @return
     */
    void AfterHandle(ServerHttpRequest request, ServerHttpResponse response);

    /**
     * 异常处理
     * @param request
     * @param response
     * @param e
     */
    void onException(ServerHttpRequest request, ServerHttpResponse response, Exception e);
}
