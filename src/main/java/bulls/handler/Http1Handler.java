
package bulls.handler;

import bulls.core.MainHttpHandler;
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
public class Http1Handler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static Log logger = LogFactory.getLog(Http1Handler.class);

    public Http1Handler() {

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //获取内存空间
        ByteBuf content = ctx.alloc().buffer();

        BullsHttpRequest request = new DefaultBullsHttpRequest(req);
        BullsHttpResponse response= new DefaultBullsHttpResponse(HTTP_1_1, OK, content);

        if (HttpUtil.is100ContinueExpected(req)) {
            response = new DefaultBullsHttpResponse(HTTP_1_1, CONTINUE);
            response.did();
        }
        MainHttpHandler.handle(request,response);

        //是否保持连接
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (!keepAlive) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("系统出错了", cause);
        ctx.close();
    }
}
