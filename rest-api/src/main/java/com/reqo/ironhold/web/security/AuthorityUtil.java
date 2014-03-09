package com.reqo.ironhold.web.security;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * Created by ilya on 3/8/14.
 */
public class AuthorityUtil {

    public static List<GrantedAuthority> getAuthorities(final LoginUser user) {
        final MutableList<RoleEnum> userRoles = ArrayIterate.select(RoleEnum.values(), new Predicate<RoleEnum>() {
            @Override
            public boolean accept(RoleEnum roleEnum) {
                return user.hasRole(roleEnum);
            }
        });

        final MutableList<GrantedAuthority> authorities = userRoles.collect(new Function<RoleEnum, GrantedAuthority>() {
            @Override
            public GrantedAuthority valueOf(final RoleEnum roleEnum) {
                return new GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return roleEnum.getAttribute();
                    }
                };
            }
        });

        return authorities;
    }

}
