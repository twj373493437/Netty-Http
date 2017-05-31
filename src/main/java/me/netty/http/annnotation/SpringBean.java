package me.netty.http.annnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记来自Spring的Bean
 * @author tianwenjian
 * @create 2017-05-18 20:05
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpringBean {
    /**
     * 名称，如果没有名称或为空，则根据类型自动获取
     * @return
     */
    String value() default "";
}
