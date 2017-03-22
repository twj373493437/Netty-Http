package me.netty.http.core.session;

import me.netty.http.ServerContext;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.http.BullsHttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.Date;

/**
 * Created by 1 on 2017/3/18.
 */
public class SessionUtils {

    public static HttpSession getNewSession(BullsHttpRequest request, BullsHttpResponse response){
        ServerContext serverContext = ServerContext.getServerContext(request);

        //TODO  获取UUID的 字串
        String sessionId = String.valueOf(new Date().getTime());

        HttpSession session = new DefaultHttpSession(serverContext, sessionId);
        serverContext.getSessionReaderWriter().writeSession(sessionId, session);

        Cookie cookie = new DefaultCookie(HttpSession.SESSION_COOKIE_NAME, sessionId);
        response.addCookie(cookie);
        return session;
    }
}
