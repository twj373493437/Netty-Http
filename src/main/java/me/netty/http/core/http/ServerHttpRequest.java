package me.netty.http.core.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import me.netty.http.core.session.HttpSession;

import java.util.Map;

/**
 * Created by 1 on 2017/3/6.
 */
public interface ServerHttpRequest extends FullHttpRequest {

    /**
     * 获取请求参数
     *
     * @param name
     * @return
     */
    String getPram(String name);

    /**
     * 获取请求域属性
     *
     * @param name
     * @return
     */
    Object getAttr(String name);

    /**
     * 设置请求域属性
     *
     * @param name
     * @return
     */
    void setAttr(String name, Object o);

    /**
     * 获取请求的path
     *
     * @return
     */
    String getRequestPath();

    /**
     * 获取cookie
     *
     * @param name
     * @return
     */
    Cookie getCookie(String name);

    /**
     * 获取所有Cookie
     *
     * @return
     */
    Map<String, Cookie> getCookie();

    /**
     * 获取Session
     *
     * @return
     */
    HttpSession getSession();

    /**
     * 获取端口
     *
     * @return
     */
    int getPort();
}
