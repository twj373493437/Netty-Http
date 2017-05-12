
package me.netty.http.handler;

import io.netty.channel.ChannelHandler;
import me.netty.http.core.ServerMainProcessor;
import me.netty.http.core.HttpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import me.netty.http.core.http.DefaultServerHttpRequest;
import me.netty.http.core.http.DefaultServerHttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http 1.1
 */
@ChannelHandler.Sharable
public class Http1Handler extends SimpleChannelInboundHandler<FullHttpRequest> implements HttpHandler {
    private static Log logger = LogFactory.getLog(Http1Handler.class);

    public Http1Handler() {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.debug("channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //获取内存空间
        ByteBuf content = ctx.alloc().buffer();

        ServerHttpRequest request = new DefaultServerHttpRequest(req);
        ServerHttpResponse response= new DefaultServerHttpResponse(HTTP_1_1, OK, content);
        if (HttpUtil.is100ContinueExpected(req)) {
            response = new DefaultServerHttpResponse(HTTP_1_1, CONTINUE);
            this.writeAndFlush(req, response, ctx);
            return;
        }

        //初始化MainProcessor
        MainProcessor processor = new ServerMainProcessor(this, request, response, ctx);
        processor.process(request,response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("系统出错了", cause);
        ctx.close();
    }

    @Override
    public void writeAndFlush(FullHttpRequest request,FullHttpResponse response,ChannelHandlerContext ctx) {

        if(ctx.isRemoved()){
            logger.error("ctx is removed,can not write response");
            return;
        }

        //是否保持连接
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }
}
