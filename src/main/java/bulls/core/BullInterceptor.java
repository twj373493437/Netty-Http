package bulls.core;

import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;

/**
 * Created by 1 on 2017/3/13.
 */
public interface BullInterceptor {

    /**
     * @param request
     * 处理之前调用
     * @param response
     * @return
     */
    boolean beforeHandle(BullsHttpRequest request, BullsHttpResponse response);

    /**
     * 处理之后调用，哦也
     * @param request
     * @param response
     * @return
     */
    void AfterHandle(BullsHttpRequest request, BullsHttpResponse response);

    /**
     * 异常处理
     * @param request
     * @param response
     * @param e
     */
    void onException(BullsHttpRequest request, BullsHttpResponse response, Exception e);
}
