package com.expansel.errai.springboot;

import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringService {
    private static final Logger logger = LoggerFactory.getLogger(SpringService.class);
    private ServerMessageBus bus;

    public String ping() {
        logger.info("bus message queues size: " + bus.getMessageQueues().size());
        return "pong";
    }

    @Autowired
    public void setBus(ServerMessageBus bus) {
        // Bus is not yet initialized at this point
        logger.info("Setting ServerMessageBus: " + bus);
        this.bus = bus;
    }
}
