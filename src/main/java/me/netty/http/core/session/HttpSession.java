package me.netty.http.core.session;

/**
 * Created by 1 on 2017/3/17.
 */
public interface HttpSession {

     String SESSION_COOKIE_NAME = "BULLSESSIONID";

    /**
     * 添加属性
     * @param name
     * @param object
     */
    void addAttr(String name, Object object);

    /**
     * 获取属性
     * @param name
     * @return
     */
    Object getAttr(String name);

    /**
     * 设置最大超时时间
     * @param second 秒
     */
    void setMaxInactiveInterval(int second);

    /**
     * 获取超时的时间点 格林尼治
     */
    long getInactiveTime();

    /**
     * 失效,并且remove掉
     */
    void invalidate();

    /**
     * 是否有效
     * @return
     */
    boolean isValidate();
}
