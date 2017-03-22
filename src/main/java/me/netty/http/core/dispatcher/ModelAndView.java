package me.netty.http.core.dispatcher;

import java.util.Map;

/**
 * 支持MVC
 * Created by 1 on 2017/3/7.
 */
public class ModelAndView {
    private Map<String, Object> attrs; //属性
    private String path;    //路径

    /**
     * Construct
     * @param path
     */
    public ModelAndView(String path){
        this.path = path;
    }

    public void setAttribute(String name, Object value){
        this.attrs.put(name, value);
    }

    public Map getAttrs(){
        return this.attrs;
    }
}
