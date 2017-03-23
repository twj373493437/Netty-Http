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

    @Mapping(value = "hello/bulls")
    public String test(BullsHttpRequest request){

        if (request == null){
            logger.error("request not init");
        }

        return "hello bulls! " + request.getPram("name");
    }

    @Mapping(value = "hello/bulls1", isAsyn = true)
    public String test1(BullsHttpRequest request){

        if (request == null){
            logger.error("request not init");
        }

        return "hello bulls! " + request.getPram("name");
    }
}
