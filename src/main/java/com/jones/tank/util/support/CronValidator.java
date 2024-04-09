package com.jones.tank.util.support;

import org.springframework.scheduling.support.CronSequenceGenerator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author yue.su
 * @date 2018年4月18日
 */
public class CronValidator implements ConstraintValidator<ValidCron, String> {

    @Override
    public boolean isValid(String cron, ConstraintValidatorContext context) {
        if(cron == null) {
            return false;
        }
        return CronSequenceGenerator.isValidExpression(cron);
    }
}
