package com.reqo.ironhold.web.security;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.utility.ArrayIterate;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.LoginUser;
import com.reqo.ironhold.web.domain.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by ilya on 3/8/14.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private IMiscIndexService miscIndexService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final LoginUser user = miscIndexService.getLoginUser(username.split("/")[0], username.split("/")[1]);


        return new User(user.getUsername(), user.getHashedPassword(), AuthorityUtil.getAuthorities(user));
    }
}
