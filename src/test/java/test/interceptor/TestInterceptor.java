package test.interceptor;

import me.netty.http.annnotation.Interceptor;
import me.netty.http.core.HttpInterceptor;
import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 1 on 2017/3/15.
 */
@Interceptor("/")
public class TestInterceptor implements HttpInterceptor {
    static Log log = LogFactory.getLog(TestInterceptor.class);
    @Override
    public boolean beforeHandle(ServerHttpRequest request, ServerHttpResponse response) {
        //log.debug("go in test interceptor before");
        return true;
    }

    @Override
    public void afterHandle(ServerHttpRequest request, ServerHttpResponse response) {

    }

    @Override
    public void onException(ServerHttpRequest request, ServerHttpResponse response, Exception e) {
        //log.error("onException", e);
    }
}
