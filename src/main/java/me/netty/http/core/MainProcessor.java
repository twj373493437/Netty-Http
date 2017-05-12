package me.netty.http.core;

import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

/**
 * 抽象层,为了以后兼容HTTP2
 * Created by 1 on 2017/3/21.
 */
public interface MainProcessor {

    /**
     * 处理，不应当负责写入被托管了方法的处理结果
     */
    void process(ServerHttpRequest request, ServerHttpResponse response);

    /**
     * 处理好了
     */
    void sendResponse();

    /**
     * 简单类型响应
     * @param response
     * @param request
     * @param status
     */
    static void productSimpleResponse(ServerHttpResponse response, ServerHttpRequest request, HttpResponseStatus status, String message){
        ByteBuf content = response.content();
        if(message == null){
            message = status.reasonPhrase();
        }
        ByteBufUtil.writeUtf8(content, message);

        response.setStatus(status);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
    }

    /**
     * 获取response
     * @return
     */
    ServerHttpResponse getResponse();

    /**
     * 获取request
     * @return
     */
    ServerHttpRequest getRequest();
}
