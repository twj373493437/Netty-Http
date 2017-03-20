package bulls.core.http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;

/**
 * Created by 1 on 2017/3/7.
 */
public interface BullsHttpResponse extends FullHttpResponse {

    /**
     * 是否已经处理
     *
     * @return
     */
    boolean isDid();

    /**
     * 表示这个返回已经被处理过了
     */
    void did();

    /**
     * 设置Cookie
     *
     * @param cookie
     */
    void addCookie(Cookie cookie);

    List<Cookie> cookies();
}
