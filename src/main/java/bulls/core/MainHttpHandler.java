package bulls.core;

import bulls.ServerContext;
import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

/**
 * Created by 1 on 2017/3/9.
 */
public class MainHttpHandler {

    public static void handle(BullsHttpRequest request, BullsHttpResponse response){

        ServerContext serverContext = ServerContext.getServerContext(request);
        List<Interceptor> list =serverContext.getInterceptors();

        //前置拦截器
        for (Interceptor interceptor : list){
            if (request.getRequestPath().startsWith(interceptor.getPath())) {
                boolean b = interceptor.beforeHandle(request, response);
                if (!b) {
                    response.did();
                }
            }
        }

        //内部服务
        if (!response.isDid() && serverContext.getDispatcher() != null){
            serverContext.getDispatcher().doService(request, response);
        }

        //静态文件
        if (!response.isDid() && serverContext.getStaticFileManager() != null){
            serverContext.getStaticFileManager().getStaticFile(request,response);
        }

        //404 错误
        if (!response.isDid()){
            getSimpleResponse(response, request, NOT_FOUND, "没有找到你想要的资源！--Bulls");
        }

        //后置拦截器
        for (Interceptor interceptor : list){
            if (request.getRequestPath().startsWith(interceptor.getPath())) {
                interceptor.beforeHandle(request, response);
            }
        }

        //处理Cookie
        if (response.cookies() != null){
            ServerCookieEncoder encoder = ServerCookieEncoder.STRICT;
            List<String> cookies = encoder.encode(response.cookies());
            for (String cookie : cookies){
                response.headers().add(HttpHeaderNames.SET_COOKIE.toString(), cookie);
            }
        }
    }

    /**
     * 简单类型响应
     * @param response
     * @param request
     * @param status
     * @return
     */
    public static void getSimpleResponse(BullsHttpResponse response, BullsHttpRequest request, HttpResponseStatus status, String message){
        ByteBuf content = response.content();
        if(message == null){
            message = status.reasonPhrase();
        }
        ByteBufUtil.writeUtf8(content, message);

        response.setStatus(status);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        response.did();
    }
}
