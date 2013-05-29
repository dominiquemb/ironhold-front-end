package com.reqo.ironhold.web.components.validators;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.PasswordField;

/**
 * User: ilya
 * Date: 5/23/13
 * Time: 10:17 PM
 */
public class PasswordValidator extends AbstractStringValidator {
    private final PasswordField confirmPassword;

    public PasswordValidator(PasswordField confirmPassword) {
        super("Password and confirm password must match");
        this.confirmPassword = confirmPassword;
    }

    @Override
    protected boolean isValidValue(String value) {
        if (confirmPassword.getValue().isEmpty() && value.isEmpty()) return true;
        if (!confirmPassword.isValid() || !confirmPassword.getValue().equals((String) value)) {
            return false;
        }
        return true;
    }
}
