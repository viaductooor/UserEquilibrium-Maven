package org.lab1505.ue.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidUsernameValidator  implements ConstraintValidator<ValidUsername,String> {

    private boolean required = false;
    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        //constraintAnnotation可以用于获取使用注解时传入的参数值
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required){
            //校验步骤
            return true;
        }else{
            //校验步骤
            return true;
        }
    }
}
