package me.netty.http.core.asyn;

import io.netty.buffer.ByteBuf;
import me.netty.http.core.MainProcessor;
import me.netty.http.core.dispatcher.Dispatcher;
import me.netty.http.core.dispatcher.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by 1 on 2017/3/22.
 */
public class ProcessRunnable implements Runnable{
    private static Log logger = LogFactory.getLog(ProcessRunnable.class);

    private MainProcessor mainProcessor;
    private Dispatcher dispatcher;
    private Function function;

    public ProcessRunnable(MainProcessor processor, Dispatcher dispatcher, Function function){
        this.mainProcessor = processor;
        this.dispatcher = dispatcher;
        this.function = function;
    }

    @Override
    public void run() {
        logger.debug("进入异步线程执行");
        dispatcher.doMethod(mainProcessor, function);

        //计数减一，release,在进入异步之前，retain
        ByteBuf byteBuf = mainProcessor.getRequest().content();
        if (byteBuf!= null && byteBuf.refCnt() > 0){
            byteBuf.release();
        }
    }

    public MainProcessor getMainProcessor(){
        return this.mainProcessor;
    }
}
