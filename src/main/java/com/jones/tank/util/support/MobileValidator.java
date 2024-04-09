package com.jones.tank.util.support;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * @author yue.su
 * @date 2018年4月18日
 */
public class MobileValidator implements ConstraintValidator<ValidMobile, String> {

    @Override
    public boolean isValid(String mobile, ConstraintValidatorContext context) {
        if(mobile == null) {
            return false;
        }
        return Pattern.matches("^1\\d{10}$", mobile);
    }

}
