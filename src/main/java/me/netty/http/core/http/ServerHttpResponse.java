package me.netty.http.core.http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;

/**
 * Created by 1 on 2017/3/7.
 */
public interface ServerHttpResponse extends FullHttpResponse {

    /**
     * 设置Cookie
     *
     * @param cookie
     */
    void addCookie(Cookie cookie);

    /**
     * 获取Cookies
     * @return
     */
    List<Cookie> cookies();

    /**
     * 写入bytes,并设置Content-length
     * @param bytes
     */
    void writeContent(byte[] bytes);
}
