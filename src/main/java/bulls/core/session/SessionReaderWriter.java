package bulls.core.session;

/**
 * Created by 1 on 2017/3/18.
 */
public interface SessionReaderWriter {

    /**
     * 写入Session值
     * @param sessionId
     *
     * @return
     */
    void writeSession(String sessionId, HttpSession session);

    /**
     * 读Session
     * @param sessionId
     *
     * @return
     */
    HttpSession readSession(String sessionId);

    /**
     * 移除
     * @param sessionId
     */
    void removeSession(String sessionId);
}
