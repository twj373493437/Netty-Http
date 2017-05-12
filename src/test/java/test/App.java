package test;

import io.netty.util.ResourceLeakDetector;
import me.netty.http.BullsHttp2Server;
import me.netty.http.ServerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;

/**
 * Hello world!
 */
public class App {
    private static Log logger = LogFactory.getLog(App.class);

    public static void main(String[] args) {

        //调试时检测内存泄漏
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);
        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

        BullsHttp2Server bullsHttp2Server = new BullsHttp2Server(8081, false);   //SSL暂时不可用
        ServerContext serverContext = bullsHttp2Server.getServerContext();

        String config = "/server.properties";
        URL path = App.class.getResource(config);
        logger.debug(path.toString());
        try {
            serverContext.initByConfigFile(config);
            bullsHttp2Server.start();
        } catch (Exception e) {
            logger.error("出现了异常", e);
        }
    }
}
