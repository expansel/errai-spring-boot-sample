package com.expansel.errai.springboot.bus;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Service
public class ErraiMessageCallbackService implements MessageCallback {
    private static final Logger logger = LoggerFactory.getLogger(ErraiMessageCallbackService.class);

    @Override
    public void callback(Message message) {
        logger.info(this + " - MessageCallback: " + message);
    }

}
