package com.expansel.errai.springboot.gwt.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.UncaughtExceptionHandler;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.expansel.errai.springboot.gwt.shared.ErraiRPCService;
import com.expansel.errai.springboot.gwt.shared.RPCResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class App extends Composite {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Inject
    private Caller<ErraiRPCService> messageServiceCaller;

    @Inject
    private MessageBus bus;

    @Inject
    private Caller<AuthenticationService> authenticationServiceCaller;

    @PostConstruct
    public void postConstruct() {
        Label pingLabel = new Label("ping...");
        RootPanel.get().add(pingLabel);
        bus.subscribe("MessageFromServer", (Message message) -> {
            Label messageLabel = new Label("Message from server: " + message.getParts().get("message"));
            RootPanel.get().add(messageLabel);
        });
    }

    @AfterInitialization
    public void afterInitialization() {
        authenticationServiceCaller.call(new RemoteCallback<User>() {

            @Override
            public void callback(User user) {
                Label userLabel = new Label("User: " + user.getIdentifier());
                RootPanel.get().add(userLabel);

                MessageBuilder.createMessage().toSubject("ErraiMessageCallbackService").signalling().noErrorHandling()
                        .sendNowWith(ErraiBus.getDispatcher());

                MessageBuilder.createMessage().toSubject("ErraiMessageCallbackService").signalling().noErrorHandling()
                        .sendNowWith(ErraiBus.getDispatcher());

                MessageBuilder.createMessage().toSubject("ErraiCommandService").command("command1").noErrorHandling()
                        .sendNowWith(ErraiBus.getDispatcher());

                MessageBuilder.createMessage().toSubject("ErraiCommandService").command("CMD2").noErrorHandling()
                        .sendNowWith(ErraiBus.getDispatcher());

                messageServiceCaller.call(new RemoteCallback<RPCResult>() {
                    @Override
                    public void callback(RPCResult result) {
                        Label resultLabel = new Label("..." + result.getResult());
                        RootPanel.get().add(resultLabel);
                        logger.info("calling trigger: " + user.getRoles());
                        messageServiceCaller.call(new RemoteCallback<Void>() {
                            @Override
                            public void callback(Void response) {
                                logger.info("done calling trigger");
                                authenticationServiceCaller.call().logout();
                            }
                        }, new ErrorCallback<Message>() {
                            @Override
                            public boolean error(Message message, Throwable t) {
                                logger.log(Level.SEVERE, "after calling trigger", t);
                                if (t instanceof UnauthenticatedException) {
                                    return true;
                                } else if (t instanceof UnauthorizedException) {
                                    Label exceptionLabel = new Label(
                                            "Did not pass admin authorization for RPC call: trigger");
                                    RootPanel.get().add(exceptionLabel);
                                    return true;
                                } else {
                                    return true;
                                }
                            }
                        }).triggerMessage();

                    }
                }).callPing();
            }
        }).getUser();
    }

    @UncaughtExceptionHandler
    private void onUncaughtException(Throwable caught) {
        try {
            throw caught;
        } catch (Throwable t) {
            Window.alert(t.getMessage());
            GWT.log("An unexpected error has occurred", t);
        }
    }

}
