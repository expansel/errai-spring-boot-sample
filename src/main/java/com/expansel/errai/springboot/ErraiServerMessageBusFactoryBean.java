package com.expansel.errai.springboot;

import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

public class ErraiServerMessageBusFactoryBean implements FactoryBean<ServerMessageBus> {
    private static final Logger logger = LoggerFactory.getLogger(ErraiServerMessageBusFactoryBean.class);

    @Override
    public ServerMessageBus getObject() throws Exception {
        logger.trace("Getting Errai ServerMessageBus");
        return ErraiServiceSingleton.getService().getBus();
    }

    @Override
    public Class<?> getObjectType() {
        return ServerMessageBus.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
