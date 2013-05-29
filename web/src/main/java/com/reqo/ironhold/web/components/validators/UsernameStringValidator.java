package com.reqo.ironhold.web.components.validators;

import com.vaadin.data.validator.RegexpValidator;

/**
 * User: ilya
 * Date: 5/27/13
 * Time: 5:15 PM
 */
public class UsernameStringValidator extends RegexpValidator {
    public UsernameStringValidator() {
        super("^([a-zA-Z0-9_\\.\\-+])+$", "Field cannot contain spaces");
    }
}
