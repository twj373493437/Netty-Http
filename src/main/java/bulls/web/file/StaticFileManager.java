package bulls.web.file;

import bulls.ServerContext;
import bulls.core.http.BullsHttpRequest;
import bulls.core.http.BullsHttpResponse;
import bulls.core.MainHttpHandler;
import bulls.web.file.cache.LruCache;
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
 * 静态文件管理
 * Created by 1 on 2017/2/20.
 */
public class StaticFileManager {

    private static Log logger = LogFactory.getLog(StaticFileManager.class);
    private static final int DEFAULT_CAPACITY = 512;  //默认512个文件，一般项目而言足够了

    private ServerContext serverContext;
    private LruCache<String, byte[]> cache;

    public StaticFileManager(ServerContext serverContext){
        this.serverContext = serverContext;
        cache = new LruCache<>(DEFAULT_CAPACITY, serverContext.getStaticFileCacheSize());
    }

    /**
     *
     * @param filePath 相对于根目录的路径
     * @return
     */
    private byte[] getBytesFromFile(String filePath) throws Exception{
        byte[] fileBytes = cache.get(filePath);
        if (null != fileBytes){
            return fileBytes;
        }

        if (serverContext.getStaticFile() == null || serverContext.getStaticFile().equals("")){
            logger.info("没有指定静态文件目录");
            return null;
        }

        File file = new File(serverContext.getStaticFile() + filePath);
        if(!file.exists() || file.isDirectory()){
           return null;
        }
        return FileUtils.readFileToByteArray(file);
    }

    /**
     * 获取文件
     * @param response
     * @param request
     * @return
     */
    public void getStaticFile(BullsHttpRequest request, BullsHttpResponse response){
        ByteBuf content = response.content();

        String path = request.getRequestPath();
        if(path.equals("/")){
            path = "/" + ServerContext.getServerContext(request).getWelcomePage();
        }

        try {
            byte[] fileByte = this.getBytesFromFile(path);
            if (fileByte == null){
                return;
            }
            content.writeBytes(fileByte);
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(path));
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            response.did();
        } catch (Exception e) {
            logger.error("读取文件发生错误:" + path, e);
            MainHttpHandler.getSimpleResponse(response,request,INTERNAL_SERVER_ERROR, "服务器内部错误：" + e.toString());
        }
    }
}