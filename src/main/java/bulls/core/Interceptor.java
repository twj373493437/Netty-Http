package bulls.core;

import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;

/**
 * Created by 1 on 2017/3/13.
 */
public interface Interceptor {

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
     * 获取要拦截的Path
     * @return
     */
    String getPath();
}
