package com.reqo.ironhold.service.resources;

import com.reqo.ironhold.storage.MiscIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Path("user")
@Component
public class UserResource {

    @Context
    Application application;

    @Autowired
    MiscIndexService miscIndexService;

    @GET
    @Path("/search/{start}/{limit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@PathParam("start") int start, @PathParam("limit") int limit, @Context SecurityContext sc) throws InterruptedException, ExecutionException, IOException {
        if (sc == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(miscIndexService.getLoginUsers("demo", start, limit)).build();
    }


}
