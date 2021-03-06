package me.netty.http.web.file;

import io.netty.buffer.ByteBuf;
import me.netty.http.core.ServerContext;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.http.ServerHttpRequest;
import me.netty.http.core.http.ServerHttpResponse;
import me.netty.http.web.file.cache.LruCache;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.InputStream;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;


/**
 * 静态文件管理,
 * TODO 考虑用缓存流的方式读文件，目前使用的是 IOUtils
 * Created by 1 on 2017/2/20.
 */
public class StaticFileManager {

    private static Log logger = LogFactory.getLog(StaticFileManager.class);
    private static final int DEFAULT_CAPACITY = 128;
    private static final String CLASS_PATH = "classpath:";

    private ServerContext serverContext;
    private LruCache<String, byte[]> cache;

    public StaticFileManager(ServerContext serverContext) {
        this.serverContext = serverContext;

        if (serverContext.getStaticFileCacheSize() != 0) {
            cache = new LruCache<>(DEFAULT_CAPACITY, serverContext.getStaticFileCacheSize());
        }
    }

    /**
     * 进过轻微的测试，好像速度没有显著改变，故其实用性有待验证
     *
     * @param filePath 相对于根目录的路径
     * @return
     */
    private byte[] getBytesFromFile(String filePath) throws Exception {
        byte[] fileBytes;

        if (serverContext.getStaticDir() == null || serverContext.getStaticDir().equals("")) {
            logger.info("没有指定静态文件目录");
            return null;
        }

//        //检查缓存中有没有
//        if(this.cache != null){
//            fileBytes = cache.get(filePath);
//            if (fileBytes != null){
//                logger.debug("get file from cache");
//                return fileBytes;
//            }
//        }

        //包里面的文件
        if (serverContext.getStaticDir().startsWith(CLASS_PATH)) {
            String dir = serverContext.getStaticDir().substring(CLASS_PATH.length());
            filePath = dir + "/" + filePath;
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(filePath);
            if (inputStream == null){
                return null;
            }
            fileBytes = IOUtils.toByteArray(inputStream);
        } else {
            File file = new File(serverContext.getStaticDir() + filePath);
            if (!file.exists() || file.isDirectory()) {
                return null;
            }
            fileBytes = FileUtils.readFileToByteArray(file);
        }

//        if (cache != null){
//            cache.put(filePath, fileBytes);
//        }

        return fileBytes;
    }

    /**
     * 获取文件
     *
     * @param response
     * @param request
     * @return
     */
    public boolean getStaticFile(ServerHttpRequest request, ServerHttpResponse response, MainProcessor mainProcessor) {
        ByteBuf content = response.content();

        String path = request.getRequestPath();
        if (path.equals("/")) {
            path = "/" + ServerContext.getServerContext(request).getWelcomePage();
        }

        try {
            byte[] fileByte = this.getBytesFromFile(path);
            if (fileByte == null) {
                return false;
            }
            content.writeBytes(fileByte);
            response.headers().set(CONTENT_TYPE, MIMEType.getType(path));
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            mainProcessor.sendResponse();
            return true;
        }catch (Exception e) {
            logger.error("读取文件发生错误:" + path, e);
            MainProcessor.productSimpleResponse(response, request, INTERNAL_SERVER_ERROR, "服务器内部错误：" + e.toString());
            mainProcessor.sendResponse();
            return true;
        }
    }
}
