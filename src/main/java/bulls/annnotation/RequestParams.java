package bulls.annnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当参数是对象时，必须使用这个注解绑定数据
 * Created by 1 on 2017/3/9.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParams {

    //是否校验
    boolean isValidate() default false;
}
