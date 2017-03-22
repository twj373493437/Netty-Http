package me.netty.http.core;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Created by 1 on 2017/3/21.
 */
public interface HttpHandler {

    /**
     * 写入Http
     */
    void writeAndFlush(FullHttpRequest request, FullHttpResponse response);
}
