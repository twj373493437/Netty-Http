package bulls.core;

import bulls.ServerContext;
import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * 基于事件的处理模式
 * Created by 1 on 2017/3/21.
 */
public class BullsMainProcessor implements MainProcessor{
    private static Log logger = LogFactory.getLog(BullsMainProcessor.class);

    private HttpHandler httpHandler;


    public BullsMainProcessor(HttpHandler httpHandler){
        this.httpHandler = httpHandler;
    }

    @Override
    public void process(BullsHttpRequest request, BullsHttpResponse response){
        ServerContext serverContext = ServerContext.getServerContext(request);
        List<Interceptor> list =serverContext.getInterceptors();

        //前置拦截器
        for (Interceptor interceptor : list){
            if (request.getRequestPath().startsWith(interceptor.getPath())) {
                boolean b = interceptor.beforeHandle(request, response);
                if (!b) {
                    this.sendResponse(request, response);
                    return;
                }
            }
        }
        try {
            //内部服务,不负责写入,以便于异步模式
            if (!response.isDid() && serverContext.getDispatcher() != null){
                if (serverContext.getDispatcher().doService(request, response, this)){
                    //this.sendResponse(request ,response);
                    return;
                }
            }

            //静态文件,不负责写入,以便于异步模式
            if (!response.isDid() && serverContext.getStaticFileManager() != null){
                if(serverContext.getStaticFileManager().getStaticFile(request,response, this)){
                    //this.sendResponse(request ,response);
                    return;
                }
            }

            //404 错误
            MainProcessor.productSimpleResponse(response, request, NOT_FOUND, "没有找到你想要的资源！--Bulls");
            this.sendResponse(request, response);

        }catch (Exception e) {
            logger.info("出现了一个错误", e);
            //异常拦截
            for (Interceptor interceptor : list) {
                if (request.getRequestPath().startsWith(interceptor.getPath())) {
                    interceptor.onExctption(request, response, e);
                    this.sendResponse(request, response);
                }
            }
        }
    }

    @Override
    public void sendResponse(BullsHttpRequest request, BullsHttpResponse response) {

        ServerContext serverContext = ServerContext.getServerContext(request);
        List<Interceptor> list =serverContext.getInterceptors();

        //处理Cookie
        if (response.cookies() != null){
            ServerCookieEncoder encoder = ServerCookieEncoder.STRICT;
            List<String> cookies = encoder.encode(response.cookies());
            for (String cookie : cookies){
                response.headers().add(HttpHeaderNames.SET_COOKIE.toString(), cookie);
            }
        }

        //后置拦截器
        for (Interceptor interceptor : list){
            if (request.getRequestPath().startsWith(interceptor.getPath())) {
                interceptor.beforeHandle(request, response);
            }
        }

        httpHandler.writeAndFlush(request, response);
    }
}
