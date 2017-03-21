package bulls.core.http;

import bulls.core.session.HttpSession;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;

/**
 * Created by 1 on 2017/3/6.
 */
public interface BullsHttpRequest extends FullHttpRequest{

    /**
     * 获取请求参数
     * @param name
     * @return
     */
    public String getPram(String name);

    /**
     * 获取请求域属性
     * @param name
     * @return
     */
    public Object getAttr(String name);

    /**
     * 设置请求域属性
     * @param name
     * @return
     */
    public void setAttr(String name, Object o);

    /**
     * 获取请求的path
     * @return
     */
    public String getRequestPath() ;

    /**
     * 获取cookie
     * @param name
     * @return
     */
    public Cookie getCookie(String name);

    /**
     * 获取所有Cookie
     * @return
     */
    public Map<String, Cookie> getCookie();

    /**
     * 获取Session
     * @return
     */
    public HttpSession getSession();

    /**
     * 获取端口
     * @return
     */
    public int getPort();
}
