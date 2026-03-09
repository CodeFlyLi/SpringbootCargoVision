package com.app.springbootcargovision.annotation;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String module() default "";
    String type() default "";
    boolean isSaveRequestData() default true;
}
