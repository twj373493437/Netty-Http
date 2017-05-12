package me.netty.http.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.LinkedList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * Created by 1 on 2017/2/26.
 */
public class DefaultServerHttpResponse extends DefaultFullHttpResponse implements ServerHttpResponse {

    private List<Cookie> cookies;

    //Construct
    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
        super(version, status, content);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
        super(version, status, validateHeaders);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders) {
        super(version, status, content, validateHeaders);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, content, validateHeaders, singleFieldHeaders);
    }

    public DefaultServerHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeaders) {
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

    @Override
    public void writeContent(byte[] bytes) {
        if(bytes == null){
            return;
        }
        this.content().writeBytes(bytes);
        this.headers().setInt(CONTENT_LENGTH, this.content().readableBytes());
    }
}
