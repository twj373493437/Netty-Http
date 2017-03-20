package bulls.core.session.men.impl;

import bulls.core.session.HttpSession;
import bulls.core.session.SessionReaderWriter;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这是基于本地内存的简单实现,这会比较耗内存
 * Created by 1 on 2017/3/18.
 */
public class MenSessionReaderWriter implements SessionReaderWriter {

    private static int refreshInterval = 30 * 60 * 1000; //刷新间隔

    public Map<String, HttpSession> sessionMap;

    public MenSessionReaderWriter(){
        this.sessionMap = new ConcurrentHashMap<>(64);

        //初始化刷新机制
        Timer timer = new Timer();
        timer.schedule(new RefreshTask(), refreshInterval, refreshInterval);
    }

    @Override
    public void writeSession(String sessionId, HttpSession session) {
        this.sessionMap.put(sessionId, session);
    }

    @Override
    public HttpSession readSession(String sessionId) {
        return this.sessionMap.get(sessionId);
    }

    @Override
    public void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
    }

    /**
     * 初始化缓存的刷新机制，这里必须要同步，避免重复初始化
     *
     *
     */
    private  void initCache() {

        Timer timer = new Timer();
        timer.schedule(new RefreshTask(), refreshInterval, refreshInterval);

    }

    class RefreshTask extends TimerTask {

        @Override
        public void run() {

            long currTime = new Date().getTime();
            // 遍历一下
            for (Map.Entry<String, HttpSession> entry : sessionMap.entrySet()) {
                HttpSession session = entry.getValue();
                long cacheTime = session.getInactiveTime();
                if (currTime > cacheTime) {
                    session.invalidate(); //先使其失效，remove在这个方法里面
                }
            }
        }
    }

    /**
     * 测试
     *
     * @param args
     *
     */
    public static void main(String[] args) {

    }
}
