package com.expansel.errai.springboot.bus;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.expansel.errai.springboot.SpringService;
import com.expansel.errai.springboot.gwt.shared.ErraiRPCService;
import com.expansel.errai.springboot.gwt.shared.RPCResult;

@Component
@Service
@Scope("session")
public class ErraiRPCServiceImpl implements ErraiRPCService {
    private static final Logger logger = LoggerFactory.getLogger(ErraiRPCServiceImpl.class);

    @Autowired
    private SpringService springService;

    private RequestDispatcher dispatcher;

    @Override
    public RPCResult callPing() {
        logger.info(this + " - callPing");
        RPCResult result = new RPCResult();
        // From printing the default to string you can see the object memory
        // reference and thus see the session scope in action, can change the
        // scope above and recompile to see others in action
        result.setResult(springService.ping() + " - " + this);
        return result;
    }

    @Override
    public void triggerMessage() {
        logger.info(this + " - triggerMessage: " + dispatcher);
        MessageBuilder.createMessage().toSubject("MessageFromServer").signalling()
                .with("message", "Passed Admin Authorization").noErrorHandling().sendNowWith(dispatcher);
    }

    @Autowired
    public void setDispatcher(RequestDispatcher dispatcher) {
        logger.info("Setting dispatcher: " + dispatcher);
        this.dispatcher = dispatcher;
    }
}
