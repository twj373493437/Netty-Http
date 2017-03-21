package twj.test;

import bulls.BullsHttp2Server;
import bulls.ServerContext;
import io.netty.util.ResourceLeakDetector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hello world!
 */
public class App {
    private static Log logger = LogFactory.getLog(App.class);

    public static void main(String[] args) {

        //调试时检测内存泄漏
        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        BullsHttp2Server bullsHttp2Server = new BullsHttp2Server(8081, false);   //SSL暂时不可用
        ServerContext serverContext = bullsHttp2Server.getServerContext();
        serverContext.setWelcomePage("welcome.html");
        serverContext.setStaticFile("E:\\development\\netty_http_static_test");
        serverContext.addPackage("twj.test");  //扫描的包

        try {
            bullsHttp2Server.start();
        } catch (Exception e) {
            logger.error("出现了异常", e);
            e.printStackTrace();
        }
    }
}