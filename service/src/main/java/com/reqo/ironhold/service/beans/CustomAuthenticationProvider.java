package com.reqo.ironhold.service.beans;

import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginChannelEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.storage.model.user.RoleEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * User: ilya
 * Date: 11/2/13
 * Time: 8:31 AM
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static Logger logger = Logger.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MiscIndexService miscIndexService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String[] chunks = authentication.getName().split("/");
        String password = authentication.getCredentials().toString();
        String clientName = chunks[0];
        String username = chunks[1];

        LoginUser user = null;
        try {
            user = miscIndexService.authenticate(clientName, username, password, LoginChannelEnum.WEB_APP, request.getRemoteAddr());
        } catch (Exception e) {
            throw new BadCredentialsException("Failed to authenticate", e) {
            };
        }

        if (user == null) {
            return null;
        }

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        for (RoleEnum role : RoleEnum.values()) {
            if (user.hasRole(role)) {
                logger.info("Adding " + role.getAttribute() + " to user authorities list");
                grantedAuths.add(new  SimpleGrantedAuthority(role.getAttribute()));
            }
        }

        logger.info("User " + authentication.getName() + " authenticated successfully from " + request.getRemoteAddr());

        return new UsernamePasswordAuthenticationToken(user, null, grantedAuths);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
