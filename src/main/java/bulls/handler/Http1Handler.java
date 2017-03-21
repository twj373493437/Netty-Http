
package bulls.handler;

import bulls.core.BullsMainProcessor;
import bulls.core.HttpHandler;
import bulls.core.http.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http 1.1
 */
public class Http1Handler extends SimpleChannelInboundHandler<FullHttpRequest> implements HttpHandler{
    private static Log logger = LogFactory.getLog(Http1Handler.class);

    private ChannelHandlerContext ctx;

    private BullsMainProcessor processor;

    public Http1Handler() {
        this.processor = new BullsMainProcessor(this);
        logger.debug("new Http1handler");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        logger.debug("channelRegistered");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //获取内存空间
        ByteBuf content = ctx.alloc().buffer();

        BullsHttpRequest request = new DefaultBullsHttpRequest(req);
        BullsHttpResponse response= new DefaultBullsHttpResponse(HTTP_1_1, OK, content);

        if (HttpUtil.is100ContinueExpected(req)) {
            response = new DefaultBullsHttpResponse(HTTP_1_1, CONTINUE);
            this.writeAndFlush(req, response);
            return;
        }
        processor.process(request,response);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("系统出错了", cause);
        ctx.close();
    }

    @Override
    public void writeAndFlush(FullHttpRequest request,FullHttpResponse response) {

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
