package com.jones.tank.util.support;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author qiong.wu
 * @date 2023年10月06日
 */
@Documented
@Constraint(validatedBy = {PasswordValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ValidPassword {

	String message() default "密码必须包含数字、字幕、符号，且长度为8-32位";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
