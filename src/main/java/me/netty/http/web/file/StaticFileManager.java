package me.netty.http.web.file;

import me.netty.http.ServerContext;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.http.BullsHttpRequest;
import me.netty.http.core.http.BullsHttpResponse;
import me.netty.http.web.file.cache.LruCache;
import io.netty.buffer.ByteBuf;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;

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

    private ServerContext serverContext;
    private LruCache<String, byte[]> cache;

    public StaticFileManager(ServerContext serverContext){
        this.serverContext = serverContext;

        if(serverContext.getStaticFileCacheSize() != 0) {
            cache = new LruCache<>(DEFAULT_CAPACITY, serverContext.getStaticFileCacheSize());
        }
    }

    /**
     *进过轻微的测试，好像速度没有显著改变，故其实用性有待验证
     * @param filePath 相对于根目录的路径
     * @return
     */
    private byte[] getBytesFromFile(String filePath) throws Exception{
        byte[] fileBytes;

        if (serverContext.getStaticFile() == null || serverContext.getStaticFile().equals("")){
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

        File file = new File(serverContext.getStaticFile() + filePath);
        if(!file.exists() || file.isDirectory()){
           return null;
        }

        fileBytes = FileUtils.readFileToByteArray(file);

//        if (cache != null){
//            cache.put(filePath, fileBytes);
//        }

        return fileBytes;
    }

    /**
     * 获取文件
     * @param response
     * @param request
     * @return
     */
    public boolean getStaticFile(BullsHttpRequest request, BullsHttpResponse response, MainProcessor mainProcessor){
        ByteBuf content = response.content();

        String path = request.getRequestPath();
        if(path.equals("/")){
            path = "/" + ServerContext.getServerContext(request).getWelcomePage();
        }

        try {
            byte[] fileByte = this.getBytesFromFile(path);
            if (fileByte == null){
                return false;
            }
            content.writeBytes(fileByte);
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(path));
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            mainProcessor.sendResponse();
            return true;
        } catch (Exception e) {
            logger.error("读取文件发生错误:" + path, e);
            MainProcessor.productSimpleResponse(response,request,INTERNAL_SERVER_ERROR, "服务器内部错误：" + e.toString());
            mainProcessor .sendResponse();
            return  true;
        }
    }
}
