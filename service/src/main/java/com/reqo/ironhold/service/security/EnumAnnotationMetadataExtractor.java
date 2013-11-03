package com.reqo.ironhold.service.security;

import com.reqo.ironhold.storage.model.user.RoleEnum;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.annotation.AnnotationMetadataExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ilya
 * Date: 11/2/13
 * Time: 3:03 PM
 */
public class EnumAnnotationMetadataExtractor implements AnnotationMetadataExtractor<SecuredEnum>
{
    @Override
    public Collection<? extends ConfigAttribute> extractAttributes(final SecuredEnum securityAnnotation)
    {
        final RoleEnum[] attributeTokens = securityAnnotation.value();
        final List<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>(attributeTokens.length);

        for (final RoleEnum token : attributeTokens)
        {
            attributes.add(new SecurityConfig(token.getAttribute()));
        }

        return attributes;
    }
}
