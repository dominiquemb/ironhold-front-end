package com.reqo.ironhold.web.security;

import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.LoginChannelEnum;
import com.reqo.ironhold.web.domain.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ilya on 3/8/14.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private IMiscIndexService miscIndexService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpReq = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        LoginUser user = miscIndexService.authenticate(authentication.getName().split("/")[0], authentication.getName().split("/")[1], authentication.getCredentials().toString(), LoginChannelEnum.WEB_APP, httpReq.getRemoteAddr());
        if (user != null) {
            return new UsernamePasswordAuthenticationToken(authentication.getName(), user.getHashedPassword(), AuthorityUtil.getAuthorities(user));
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
