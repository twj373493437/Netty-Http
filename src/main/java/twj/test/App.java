package twj.test;

import io.netty.util.ResourceLeakDetector;
import me.netty.http.BullsHttp2Server;
import me.netty.http.ServerContext;
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
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

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
