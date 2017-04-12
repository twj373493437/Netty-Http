package me.netty.http;

import me.netty.http.core.dispatcher.Dispatcher;
import me.netty.http.core.BullInterceptor;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.session.SessionListener;
import me.netty.http.core.session.SessionReaderWriter;
import me.netty.http.core.session.men.impl.MenSessionReaderWriter;
import me.netty.http.web.file.StaticFileManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**服务上下文, 提供了在一个程序里启动多个独立上下文服务的能力
 * Created by 1 on 2017/2/25.
 */
public class ServerContext {

    private static String WELCOME_PAGE = "firstPage";
    private static String STATIIC_FOLDER = "staticFolder";
    private static String SACAN_PACKAGE = "packages";

    private static Log logger = LogFactory.getLog(ServerContext.class);
    //常量约束

    //Context map,以支持多个server,同时又能在其他类中获取Context
    private static Map<Integer, ServerContext> contexts = new ConcurrentHashMap<>(8);

    //Context域数据存储
    private final Map<String, Object> attrs = new ConcurrentHashMap<>();

    //静态文件处理
    private StaticFileManager staticFileManager;  //静态文件
    private String staticFile;
    private String welcomePage;
    private int staticFileCacheSize;

    //路由
    private List<String> scanPackages;
    private Dispatcher dispatcher;

    /**
     * session
     */
    private SessionReaderWriter sessionReaderWriter;
    private SessionListener sessionListener;

    //拦截器
    private List<BullInterceptor> bullInterceptors;

    private ServerContext(){
        scanPackages = new ArrayList<>();
        bullInterceptors = new ArrayList<>(8);  //一般不会超过8个
    }

    /**
     * 添加拦截器
     * @param bullInterceptor
     */
    public void addInterceptor(BullInterceptor bullInterceptor){
        if (bullInterceptor != null){
            this.bullInterceptors.add(bullInterceptor);
        }
    }

    /**
     * 获取拦截器
     * @return
     */
    public List<BullInterceptor> getBullInterceptors(){
        return this.bullInterceptors;
    }

    public Object getAttr(String key){
        return attrs.get(key);
    }

    public void setAttr(String key, Object o){
        attrs.put(key, o);
    }

    /**
     * 初始化Context，在Server start的时候会调用，不必手动调用
     */
    public void initContext(){
        if(this.scanPackages.size() > 0){
            dispatcher = new Dispatcher(this);
            try {
                dispatcher.addPackages(this.scanPackages);
            } catch (Exception e) {
                logger.error("初始化失败",e);
            }
        }

        if(this.staticFile != null && !this.staticFile.equals( "")){
            this.staticFileManager = new StaticFileManager(this);
        }

        //如果没有指定的 SessionReaderWriter，则用默认的内存实现
        if (this.sessionReaderWriter == null){
            this.sessionReaderWriter = new MenSessionReaderWriter();
        }
    }

    /**
     * 利用监听的端口号获取个服务
     * @param port
     * @return
     */
    public static ServerContext getServerContext(Integer port){
        ServerContext context = contexts.get(port);
        if(context == null){
            context = new ServerContext();
            contexts.put(port,context);
        }
        return context;
    }

    /**
     * 利用端口设置context
     * @param port
     */
    public static void addServerContext(Integer port){
        contexts.put(port, new ServerContext());
    }

    /**
     * 利用request获取
     * @param request
     * @return
     */
    public static ServerContext getServerContext(BullsHttpRequest request){
        int port = request.getPort();
        return ServerContext.getServerContext(port);
    }

    /**
     * 获取文件管理器
     * @return
     */
    public StaticFileManager getStaticFileManager() {
        return staticFileManager;
    }

    public String getWelcomePage() {
        return welcomePage;
    }

    public void setWelcomePage(String welcomePage) {
        this.welcomePage = welcomePage;
    }

    /**
     * 添加要扫描的包
     */
    public ServerContext addPackage(String ... packageName) {
        if (packageName == null || packageName.length == 0){
            return this;
        }
        for(String name : packageName){
            this.scanPackages.add(name);
        }
        return this;
    }

    /**
     * 获取路由器
     * @return
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * 获取静态文件路径
     * @return
     */
    public String getStaticFile() {
        return staticFile;
    }

    /**
     * 设置静态文件路径
     * @param staticFile
     */
    public ServerContext setStaticFile(String staticFile) {
        this.staticFile = staticFile;
        return this;
    }

    /**
     * 设置静态文件路径
     * @param staticFile
     * @param staticFileCacheSize
     */
    public ServerContext setStaticFile(String staticFile, int staticFileCacheSize) {
        this.staticFile = staticFile;
        this.staticFileCacheSize = staticFileCacheSize;
        return this;
    }

    public int getStaticFileCacheSize() {
        return staticFileCacheSize;
    }

    /**
     * 获取Session读写
     * @return
     */
    public  SessionReaderWriter getSessionReaderWriter(){
        return this.sessionReaderWriter;
    }

    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    public SessionListener getSessionListener() {
        return sessionListener;
    }

    /**
     * 通过配置文件配置
     * @param path
     */
    public void initByPlaceHolder(String path){
        Properties pro = new Properties();
        try (
                FileInputStream in = new FileInputStream("a.properties")
        ){
            pro.load(in);
            this.setStaticFile(pro.getProperty(STATIIC_FOLDER));
            this.setWelcomePage(pro.getProperty(WELCOME_PAGE));

            String p = pro.getProperty(SACAN_PACKAGE);
            if (p != null) {
                String[] packages = p.split(",");
                for(String s : packages) {
                    this.addPackage(s);
                }
            }

            logger.info("读取配置文件" + path + ".");
        }catch (Exception e){
            logger.error(e);
        }

    }
}
