package twj.test.interceptor;

import bulls.core.Interceptor;
import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 1 on 2017/3/15.
 */
public class TestInterceptor implements Interceptor {
    static Log log = LogFactory.getLog(TestInterceptor.class);
    @Override
    public boolean beforeHandle(BullsHttpRequest request, BullsHttpResponse response) {
        log.debug("go in test interceptor before");
        return true;
    }

    @Override
    public void AfterHandle(BullsHttpRequest request, BullsHttpResponse response) {
        log.debug("go in test interceptor after");
    }

    @Override
    public String getPath() {
        return "/";
    }
}
