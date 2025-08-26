package com.xiaomimall.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {
    Class<? extends Enum<?>> enumClass();
    String message() default "无效的枚举值";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}