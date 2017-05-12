package test;

import io.netty.util.ResourceLeakDetector;
import me.netty.http.HttpServer;
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

        HttpServer httpServer = new HttpServer(8081, false);   //SSL暂时不可用
        ServerContext serverContext = httpServer.getServerContext();

        String config = "/server.properties";
        URL path = App.class.getResource(config);
        logger.debug(path.toString());
        try {
            serverContext.initByConfigFile(config);
            httpServer.start();
        } catch (Exception e) {
            logger.error("出现了异常", e);
        }
    }
}
