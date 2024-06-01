package com.lunark.lunark.validation;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.service.IAccountService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
@Component
public class AccountExistsConstraintValidator implements ConstraintValidator<AccountExistsConstraint, UUID> {
    @Autowired
    IAccountService accountService;

    @Override
    public void initialize(AccountExistsConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(UUID id, ConstraintValidatorContext constraintValidatorContext) {
        Optional<Account> account = accountService.find(id);
        if (account.isEmpty()) {
            return false;
        }
        return true;
    }
}
