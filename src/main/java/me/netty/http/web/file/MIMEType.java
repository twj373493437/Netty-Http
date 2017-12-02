package me.netty.http.web.file;

import java.util.HashMap;
import java.util.Map;

/**
 * MIME
 */
public class MIMEType {

    private static final Map<String, String> TYPES = new HashMap<>(16);

    private static final String DEFAULT_TYPE = "application/octet-stream";

    static {
        TYPES.put("css", "text/css");
        TYPES.put("js", "application/x-javascript");
        TYPES.put("html", "text/html");
        TYPES.put("jpg", "image/jpeg");
        TYPES.put("gif", "image/gif");
        TYPES.put("png", "image/png");
    }

    /**
     * get type
     */
    public static String getType(String path){
        int index = path.lastIndexOf(".");
        if (index < 0){
            return DEFAULT_TYPE;
        }

        String suffix = path.substring(index + 1);
        String type = TYPES.get(suffix);

        if (type == null){
            return DEFAULT_TYPE;
        }

        return type;
    }
}
