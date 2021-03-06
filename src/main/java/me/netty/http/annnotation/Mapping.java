package me.netty.http.annnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 1 on 2017/2/26.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    String value();  //路径
    String method() default "";//方法
    boolean isAsyn() default false; //是否用新的线程池来处理这个请求，而不是用Netty work线程
}
