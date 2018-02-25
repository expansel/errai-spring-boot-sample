package com.expansel.errai.springboot;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Path("/hello")
public class HelloWorldEndpoint {
    @Autowired
    private CurrentAuthenticationProvider currentAuthenticationProvider;
    
    @GET
    @Path("/world")
    public String test() {
        return "Hello world! " + SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GET
    @Path("/user")
    public String user() {
        return "Hello user " + currentAuthenticationProvider.currentAuthentication().getName();
    }
    
}