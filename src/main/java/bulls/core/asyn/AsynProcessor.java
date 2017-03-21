package bulls.core.asyn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 1 on 2017/3/21.
 */
public class AsynProcessor {

    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

}
