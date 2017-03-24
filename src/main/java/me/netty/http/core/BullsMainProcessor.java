package me.netty.http.core;

import io.netty.channel.ChannelHandlerContext;
import me.netty.http.ServerContext;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.http.BullsHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * 基于事件的处理模式,
 *
 * Created by 1 on 2017/3/21.
 */
public class BullsMainProcessor implements MainProcessor{
    private static Log logger = LogFactory.getLog(BullsMainProcessor.class);

    private HttpHandler httpHandler;
    private BullsHttpResponse response;
    private BullsHttpRequest request;
    private ChannelHandlerContext ctx;  //这里保存ctx

    public BullsMainProcessor(HttpHandler httpHandler, BullsHttpRequest request, BullsHttpResponse response, ChannelHandlerContext ctx){
        this.httpHandler = httpHandler;
        this.request = request;
        this.response = response;
        this.ctx = ctx;
    }

    @Override
    public void process(BullsHttpRequest request, BullsHttpResponse response){
        ServerContext serverContext = ServerContext.getServerContext(request);
        List<BullInterceptor> list =serverContext.getBullInterceptors();

        //前置拦截器
        for (BullInterceptor bullInterceptor : list){
            boolean b = bullInterceptor.beforeHandle(request, response);
            if (!b) {
                this.sendResponse();
                return;
            }
        }
        try {
            //内部服务,不负责写入,以便于异步模式
            if (serverContext.getDispatcher() != null){
                if (serverContext.getDispatcher().doService(request, response, this)){
                    //this.sendResponse(request ,response);
                    return;
                }
            }

            //静态文件,不负责写入,以便于异步模式
            if (serverContext.getStaticFileManager() != null){
                if(serverContext.getStaticFileManager().getStaticFile(request,response, this)){
                    return;
                }
            }

            //404 错误
            MainProcessor.productSimpleResponse(response, request, NOT_FOUND, "没有找到你想要的资源！--Bulls");
            this.sendResponse();

        }catch (Exception e) {
            logger.info("出现了一个错误", e);
            //异常拦截
            for (BullInterceptor bullInterceptor : list) {

                bullInterceptor.onException(request, response, e);
                this.sendResponse();

            }
        }
    }

    @Override
    public void sendResponse() {

        ServerContext serverContext = ServerContext.getServerContext(request);
        List<BullInterceptor> list =serverContext.getBullInterceptors();

        //处理Cookie
        if (response.cookies() != null){
            ServerCookieEncoder encoder = ServerCookieEncoder.STRICT;
            List<String> cookies = encoder.encode(response.cookies());
            for (String cookie : cookies){
                response.headers().add(HttpHeaderNames.SET_COOKIE.toString(), cookie);
            }
        }

        //后置拦截器
        for (BullInterceptor bullInterceptor : list){

            bullInterceptor.beforeHandle(request, response);

        }

        httpHandler.writeAndFlush(request, response, this.ctx);
    }

    @Override
    public BullsHttpResponse getResponse() {
        return this.response;
    }

    @Override
    public BullsHttpRequest getRequest() {
        return this.request;
    }
}
