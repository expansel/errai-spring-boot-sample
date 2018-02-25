package com.expansel.errai.springboot;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {
    
    public JerseyConfig() {
        register(HelloWorldEndpoint.class);
    }
}