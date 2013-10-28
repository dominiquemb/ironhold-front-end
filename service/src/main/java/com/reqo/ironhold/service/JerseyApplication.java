package com.reqo.ironhold.service;

import com.reqo.ironhold.service.resources.ImportPSTResource;
import com.reqo.ironhold.service.resources.LoginResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * User: ilya
 * Date: 10/27/13
 * Time: 5:23 PM
 */
public class JerseyApplication extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet();
        s.add(ImportPSTResource.class);
        s.add(LoginResource.class);
        return s;
    }
}