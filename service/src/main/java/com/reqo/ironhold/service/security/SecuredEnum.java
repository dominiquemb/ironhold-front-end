package com.reqo.ironhold.service.security;

import com.reqo.ironhold.storage.model.user.RoleEnum;

import java.lang.annotation.*;

/**
 * User: ilya
 * Date: 11/2/13
 * Time: 3:02 PM
 */
@Target({ ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SecuredEnum
{
    public RoleEnum[] value();
}
