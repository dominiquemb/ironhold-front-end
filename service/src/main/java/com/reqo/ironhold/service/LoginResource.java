package com.reqo.ironhold.service;

import com.reqo.ironhold.servicemodel.LoginRequest;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("login")
public class LoginResource {

    @Context
    Application application;

    @Autowired
    MiscIndexService miscIndexService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response login(String json)
            throws Exception {

        LoginRequest loginRequest = LoginRequest.deserialize(json);

        LoginUser results = miscIndexService.authenticate(loginRequest.getClient(), loginRequest.getUsername(), loginRequest.getPassword());

        if (results != null) {
            return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN_TYPE).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

}
