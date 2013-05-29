package com.reqo.ironhold.web.components.validators;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;

/**
 * User: ilya
 * Date: 5/23/13
 * Time: 10:31 PM
 */
public class EmailListValidator extends AbstractStringValidator {
    /**
     * Constructs a validator for strings.
     * <p/>
     * <p>
     * Null and empty string values are always accepted. To reject empty values,
     * set the field being validated as required.
     * </p>
     *
     * @param errorMessage the message to be included in an {@link com.vaadin.data.Validator.InvalidValueException}
     *                     (with "{0}" replaced by the value that failed validation).
     */
    public EmailListValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidValue(String value) {
        if (value.trim().isEmpty()) return true;
        EmailValidator ev = new EmailValidator("Contains invalid email address");
        for (String address : value.split("\n")) {

            if (!ev.isValid(address)) return false;

        }
        return true;
    }

}
