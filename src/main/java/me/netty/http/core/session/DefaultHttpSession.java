package me.netty.http.core.session;

import me.netty.http.core.ServerContext;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 1 on 2017/3/18.
 */
public class DefaultHttpSession implements HttpSession {

    /**
     * 默认的超时时间 30min
     */
    private static final int DEFAULT_MAX_TIME = 30 * 60 * 1000;

    /**
     * 属性
     */
    private Map<String, Object> attrs;

    /**
     * 超时时间（毫秒） 这里存毫秒节省读取的时候的计算时间
     */
    private long maxInactiveInterval;

    /**
     * 最后的访问时间 格林尼治
     */
    private long lastAccessTime;

    //是否有效,保持内存可见
    private volatile boolean validate;

    /**
     * sessionId
     */
    private String sessionId;

    private ServerContext serverContext;

    public DefaultHttpSession(ServerContext serverContext, String sessionId){
        this.attrs = new ConcurrentHashMap<>();
        this.lastAccessTime = new Date().getTime();
        this.maxInactiveInterval = DEFAULT_MAX_TIME;
        this.validate = true;
        this.sessionId = sessionId;
        this.serverContext = serverContext;

        if (serverContext.getSessionListener() != null){
            serverContext.getSessionListener().onCreate(this);
        }
    }

    @Override
    public void addAttr(String name, Object object) {
        this.attrs.put(name, object);
    }

    @Override
    public Object getAttr(String name) {
        return this.attrs.get(name);
    }

    @Override
    public void setMaxInactiveInterval(int second) {
        this.maxInactiveInterval = second * 1000;
    }

    @Override
    public long getInactiveTime() {
        return this.lastAccessTime + this.maxInactiveInterval;
    }

    @Override
    public void invalidate() {
        if (serverContext.getSessionListener() != null){
            serverContext.getSessionListener().onInvalidate(this);
        }

        this.validate = false;
        this.serverContext.getSessionReaderWriter().removeSession(this.sessionId);
    }

    @Override
    public boolean isValidate() {
        return this.validate;
    }
}
