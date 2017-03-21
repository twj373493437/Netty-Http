package bulls.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by 1 on 2017/2/26.
 */
public class DefaultBullsHttpResponse extends DefaultFullHttpResponse implements BullsHttpResponse{

    private List<Cookie> cookies;

    //Construct
    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
        super(version, status, content);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
        super(version, status, validateHeaders);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders) {
        super(version, status, content, validateHeaders);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, content, validateHeaders, singleFieldHeaders);
    }

    public DefaultBullsHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeaders) {
        super(version, status, content, headers, trailingHeaders);
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (this.cookies == null) {
            this.cookies = new LinkedList<>();
        }
        if (cookie != null) {
            this.cookies.add(cookie);
        }
    }

    @Override
    public List<Cookie> cookies() {
        return this.cookies;
    }
}
