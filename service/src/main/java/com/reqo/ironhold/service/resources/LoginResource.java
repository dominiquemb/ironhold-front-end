package com.reqo.ironhold.service.resources;

import com.reqo.ironhold.servicemodel.LoginRequest;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginChannelEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("login")
@Component
public class LoginResource {

    @Context
    Application application;

    @Autowired
    MiscIndexService miscIndexService;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(LoginRequest loginRequest, @Context HttpServletRequest request)
            throws Exception {

        String ip = "unknown";
        if (request != null) {
            ip = request.getRemoteAddr();
        }

        LoginUser results = miscIndexService.authenticate(loginRequest.getClient(), loginRequest.getUsername(), loginRequest.getPassword(), LoginChannelEnum.fromString(loginRequest.getLoginChannel()), ip);

        if (results != null) {
            return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN_TYPE).entity("Login successful").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("Login failed").build();
        }
    }

}
