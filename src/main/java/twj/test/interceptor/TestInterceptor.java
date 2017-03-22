package twj.test.interceptor;

import me.netty.http.annnotation.Interceptor;
import me.netty.http.core.BullInterceptor;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.http.BullsHttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 1 on 2017/3/15.
 */
@Interceptor("/")
public class TestInterceptor implements BullInterceptor {
    static Log log = LogFactory.getLog(TestInterceptor.class);
    @Override
    public boolean beforeHandle(BullsHttpRequest request, BullsHttpResponse response) {
        //log.debug("go in test interceptor before");
        return true;
    }

    @Override
    public void AfterHandle(BullsHttpRequest request, BullsHttpResponse response) {
        //log.debug("go in test interceptor after");
    }

    @Override
    public void onException(BullsHttpRequest request, BullsHttpResponse response, Exception e) {
        //log.error("onException", e);
    }
}
