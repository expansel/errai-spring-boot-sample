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
import com.google.gwt.json.client.JSONException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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
        bus.subscribe("MessageFromServer", (Message message) -> {
            Window.alert("Message from server: " + message.getParts().get("message"));
        });

    }

    private void initBtns() {
        Button btnMessageCallback = new Button("MessageCallback");
        btnMessageCallback.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiMessageCallbackService").signalling().noErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
            Window.alert("See log statements for printed message from MessageCallback implementation");
        });
        RootPanel.get().add(btnMessageCallback);

        Button btnCommand1 = new Button("CommandService: command1");
        btnCommand1.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiCommandService").command("command1").noErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
            Window.alert(
                    "See log statements for printed message from Service with @Command annotated methods: command name equals method name");
        });
        RootPanel.get().add(btnCommand1);

        Button btnCommand2 = new Button("CommandService: command2");
        btnCommand2.addClickHandler(event -> {
            MessageBuilder.createMessage().toSubject("ErraiCommandService").command("CMD2").noErrorHandling()
                    .sendNowWith(ErraiBus.getDispatcher());
            Window.alert(
                    "See log statements for printed message from Service with @Command annotated methods: command name specified in annotation");
        });
        RootPanel.get().add(btnCommand2);

        Button btnRPCPing = new Button("RPC: ping");
        btnRPCPing.addClickHandler(event -> {
            messageServiceCaller.call(new RemoteCallback<RPCResult>() {
                @Override
                public void callback(RPCResult result) {
                    Window.alert("Ping result: " + result.getResult());
                }
            }).callPing();
        });
        RootPanel.get().add(btnRPCPing);

        Button btnRPCTrigger = new Button("ADMIN ONLY RPC: trigger");
        btnRPCTrigger.addClickHandler(event -> {
            messageServiceCaller.call(new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                    logger.info("done calling trigger");
                    // this would have trigger bus subscription
                    // MessageFromServer if user had admin role
                }
            }, new ErrorCallback<Message>() {
                @Override
                public boolean error(Message message, Throwable t) {
                    logger.log(Level.SEVERE, "after calling trigger", t);
                    if (t instanceof UnauthenticatedException) {
                        return true;
                    } else if (t instanceof UnauthorizedException) {
                        Window.alert("Did not pass admin authorization for RPC call: trigger");
                        return true;
                    } else {
                        return true;
                    }
                }
            }).triggerMessage();
        });
        RootPanel.get().add(btnRPCTrigger);

        Button btnLogout = new Button("Logout");
        btnLogout.addClickHandler(event -> {
            authenticationServiceCaller.call().logout();
        });
        RootPanel.get().add(btnLogout);
    }

    @AfterInitialization
    public void afterInitialization() {
        authenticationServiceCaller.call(new RemoteCallback<User>() {
            @Override
            public void callback(User user) {
                Window.alert("User logged in: username=" + user.getIdentifier() + " roles=" + user.getRoles());
                initBtns();
            }
        }).getUser();
    }

    @UncaughtExceptionHandler
    private void onUncaughtException(Throwable caught) {
        try {
            if (caught instanceof JSONException
                    && caught.getMessage().contains("unexpected character at line 1 column 1 of the JSON data")) {
                // Not sure how best to deal with this when using MessageBus
                logger.info("JsonParseException usually as a result of failed redirect to login from message bus");
                Window.Location.assign("/");
            } else {
                throw caught;
            }
        } catch (Throwable t) {
            Window.alert(t.getMessage());
            GWT.log("An unexpected error has occurred", t);
        }
    }

}
