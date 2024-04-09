package com.jones.tank.util.support;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * @author qiong.wu
 * @date 2023年10月06日
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private static final String PATTERN_PASSWORD = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,32}";
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if(password == null) {
            return false;
        }
        return Pattern.matches(PATTERN_PASSWORD, password);
    }

}
