package com.expansel.errai.springboot.bus;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Service
public class ErraiCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ErraiCommandService.class);

    @Command
    public void command1(Message message) {
        logger.info(this + " - command1");
    }

    @Command("CMD2")
    public void command2(Message message) {
        logger.info(this + " - command2");
    }

}
