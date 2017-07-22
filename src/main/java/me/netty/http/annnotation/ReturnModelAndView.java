package me.netty.http.annnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示返回的是Model, 返回Model时，请把返回类型定义为 ReturnModelAndView
 * Created by 1 on 2017/3/7.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnModelAndView {
    String value() default "";
}
