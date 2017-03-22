package me.netty.http.core.session;

/**
 * Session 的监听器
 * Created by 1 on 2017/3/18.
 */
public interface SessionListener {

    /**
     * 创建时
     * @param session
     */
    void onCreate(HttpSession session);

    /**
     * 失效时
     * @param session
     * @return
     */
    void onInvalidate(HttpSession session);
}
