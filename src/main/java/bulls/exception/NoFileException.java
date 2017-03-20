package bulls.exception;

/**
 * 自定义的业务异常,不爬栈,不爬栈的异常难以调试
 * Created by 1 on 2017/3/6.
 */
public class NoFileException extends RuntimeException {

    public NoFileException(String message){
        super(message, null, false, false);
    }
}
