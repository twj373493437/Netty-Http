package twj.test.controller;

import me.netty.http.annnotation.Controller;
import me.netty.http.annnotation.Mapping;
import me.netty.http.core.http.BullsHttpRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 1 on 2017/3/7.
 */
@Controller
public class TestController {

    private static Log logger = LogFactory.getLog(TestController.class);

    /**
     * 测这个的时候 JMeter直接卡住
     * @param request
     * @return
     */
    @Mapping(value = "hello/bulls")
    public String test(BullsHttpRequest request){

        if (request == null){
            logger.error("request not init");
        }

        return "hello bulls! " + request.getPram("name");
    }

    /**
     * 经过测试，客户机和服务在同一台i5 4590上的一分钟请求数50万
     * @param request
     * @return
     */
    @Mapping(value = "hello/bulls1", isAsyn = true)
    public String test1(BullsHttpRequest request){

        if (request == null){
            logger.error("request not init");
        }

        return "hello bulls! " + request.getPram("name");
    }

    //请求文件时只有5万
}
