package me.netty.http.core.http;

import me.netty.http.ServerContext;
import me.netty.http.core.session.HttpSession;
import me.netty.http.core.session.SessionReaderWriter;
import me.netty.http.core.session.SessionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.*;
import io.netty.handler.codec.http.cookie.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 参数和一些请求数据 以 需要时获取为原则实现
 * Created by 1 on 2017/2/26.
 */
public class DefaultBullsHttpRequest extends DefaultFullHttpRequest implements BullsHttpRequest{

    private static Log logger = LogFactory.getLog(DefaultBullsHttpRequest.class);

    //请求参数
    private Map<String, String> params;
    //请求域的属性
    private Map<String, Object> attrs;

    private String requestPath;

    private Map<String,Cookie> cookies;

    private BullsHttpResponse response;

    private int port;

    //Constructor
    public DefaultBullsHttpRequest(FullHttpRequest request) {
        super(request.protocolVersion(), request.method(), request.uri(), request.content(), request.headers(), request.trailingHeaders());
        attrs = new LinkedHashMap<>();
        this.port = 0;
    }

    public DefaultBullsHttpRequest(FullHttpRequest request, BullsHttpResponse response) {
        this(request);
        this.response = response;
    }

    @Override
    public String getPram(String name){
        if (params == null){
            this.initParams();
        }
        return this.params.get(name);
    }

    @Override
    public Object getAttr(String name){
        return this.attrs.get(name);
    }

    @Override
    public void setAttr(String name , Object o) {
        this.attrs.put(name, o);
    }

    @Override
    public String getRequestPath() {
        if (requestPath == null){
            String uri = this.uri();
            String[] uriArray = uri.split("[?]", 1);
            this.requestPath = uriArray[0];
        }
        return requestPath;
    }

    @Override
    public Cookie getCookie(String name) {
        if (this.cookies == null){
           this.initCookies();
        }
        return this.cookies.get(name);
    }

    @Override
    public Map<String,Cookie> getCookie() {
        return null;
    }

    @Override
    public HttpSession getSession() {
       ServerContext serverContext = ServerContext.getServerContext(this);
       SessionReaderWriter sessionReaderWriter = serverContext.getSessionReaderWriter();
       Cookie sessionCookie = this.cookies.get(HttpSession.SESSION_COOKIE_NAME);

        HttpSession session;
       if(sessionCookie != null) {
           session = sessionReaderWriter.readSession(sessionCookie.value());
           if (session == null){
               session = SessionUtils.getNewSession(this, response);
           }else if (!session.isValidate()){
               session = SessionUtils.getNewSession(this, response);
           }

           return sessionReaderWriter.readSession(sessionCookie.value());
       }else {

          session = SessionUtils.getNewSession(this, response);
       }
       return session;
    }

    @Override
    public int getPort() {
        if (this.port == 0) {
            HttpHeaders headers = this.headers();
            String host = headers.get(HttpHeaderNames.HOST);
            String port = host.split(":")[1];

            this.port = Integer.valueOf(port);
        }
        return this.port;
    }

    /**
     * 初始化Cookies
     */
    private void initCookies(){
        this.cookies = new LinkedHashMap<>();
        String cookieStr = this.headers().get(HttpHeaderNames.COOKIE);
        if (cookieStr != null && !cookieStr.equals("")) {
            ServerCookieDecoder decoder = ServerCookieDecoder.STRICT;
            Set<Cookie> cookiesSet = decoder.decode(this.headers().get(HttpHeaderNames.COOKIE));

            for (Cookie cookie : cookiesSet){
                this.cookies.put(cookie.name(), cookie);
            }
        }
    }

    /**
     * 初始化参数信息
     *
     */
    private void initParams(){
        params = new LinkedHashMap<>(8);

        //处理uri后面带的参数
        String uri = this.uri();
        String[] uriArray = uri.split("[?]", 1);
        this.requestPath = uriArray[0];
        if (uriArray.length == 2){
            String[] params = uriArray[1].split("&");
            for (String s : params){
                String[] param = s.split("=");
                if (param.length == 2){
                    this.params.put(param[0], param[1]);
                }
            }
        }

        if (this.method() == HttpMethod.POST) {
            initParamsInBody();
        }
    }

    /**
     * 获取Post请求body的参数
     *
     */
    private void initParamsInBody(){
        String contentType = this.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentType == null){
            return;
        }

        //传递的值信息
        if(contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())){
            //String[] strings = contentType.split(";");
            //String requestCharset = strings[1].split("=")[1];

            ByteBuf byteBuf = this.content();
            byteBuf.retain();
            try {
                String body = new String(ByteBufUtil.getBytes(byteBuf), "UTF-8");
                String[] params = body.split("&");
                for (String s : params){
                    String[] param = s.split("=");
                    if (param.length == 2){
                        this.params.put(param[0], param[1]);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("获取请求体信息出错", e);
            }
        }
    }
}
