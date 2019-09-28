package org.lab1505.ue.web.validator;


import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ValidUsernameValidator.class}) //要定义一个xxValidator，里面包含具体的判断方法
public @interface ValidUsername {
    boolean required() default true; //默认要求不为空

    String message() default "Invalid Username"; //校验失败时输出的信息

    Class<?>[] groups() default { }; //不用管

    Class<? extends Payload>[] payload() default { }; //不用管
}
