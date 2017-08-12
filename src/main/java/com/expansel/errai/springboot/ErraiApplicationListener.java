package com.expansel.errai.springboot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.RPCEndpointFactory;
import org.jboss.errai.bus.server.io.RemoteServiceCallback;
import org.jboss.errai.bus.server.io.ServiceInstanceProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.util.NotAService;
import org.jboss.errai.bus.server.util.ServiceTypeParser;
import org.jboss.errai.codegen.util.ProxyUtil;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.framework.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class ErraiApplicationListener implements BeanFactoryPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ErraiApplicationListener.class);
    private List<ServiceImplementation> services = new ArrayList<ServiceImplementation>();

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("ContextClosedEvent");
        MessageBus bus = ErraiServiceSingleton.getService().getBus();
        if (bus != null) {
            for (ServiceImplementation serviceImplementation : services) {
                String subject = serviceImplementation.getSubject();
                logger.info("Unsubscribing " + subject);
                bus.unsubscribeAll(subject);
            }
        }
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("ContextRefreshedEvent");

        ApplicationContext applicationContext = event.getApplicationContext();
        logger.info("Found " + services.size() + " services");

        ErraiServiceSingleton.registerInitCallback(new ErraiServiceSingleton.ErraiInitCallback() {
            @SuppressWarnings("rawtypes")
            @Override
            public void onInit(ErraiService service) {
                logger.info("Subscribing " + services.size() + " services.");
                for (ServiceImplementation serviceImplementation : services) {
                    subscribe(applicationContext, service, serviceImplementation);
                }
            }
        });
    }

    public static class ServiceImplementation {
        private ServiceTypeParser serviceTypeParser;
        private String objectName;

        public ServiceImplementation(ServiceTypeParser serviceTypeParser, String objectName) {
            super();
            this.serviceTypeParser = serviceTypeParser;
            this.objectName = objectName;
        }

        public ServiceTypeParser getServiceTypeParser() {
            return serviceTypeParser;
        }

        public String getBeanName() {
            return objectName;
        }

        public boolean isRPC() {
            return serviceTypeParser.getRemoteImplementation() != null;
        }

        public String getSubject() {
            // not sure why ServiceTypeParser does not return correct RPC name
            Class<?> remoteInterface = serviceTypeParser.getRemoteImplementation();
            if (remoteInterface != null) {
                return remoteInterface.getName() + ":RPC";
            }
            return serviceTypeParser.getServiceName();
        }
    }

    @SuppressWarnings("rawtypes")
    private void subscribe(final ApplicationContext applicationContext, final ErraiService service,
            final ServiceImplementation serviceImplementation) {
        final ServerMessageBus bus = ErraiServiceSingleton.getService().getBus();
        ServiceTypeParser serviceTypeParser = serviceImplementation.getServiceTypeParser();

        if (!serviceImplementation.isRPC()) {
            String subject = serviceImplementation.getSubject();
            logger.info("Subscribing MessageCallback " + subject);
            // All the Errai supporting classes seem to be geared to having a
            // singleton here and Errai's CDI implementation also only supports
            // singletons for non-rpc's
            Object instance = applicationContext.getBean(serviceImplementation.getBeanName());
            final MessageCallback callback = serviceTypeParser.getCallback(instance);
            if (callback != null) {
                if (serviceTypeParser.isLocal()) {
                    bus.subscribeLocal(subject, callback);
                } else {
                    bus.subscribe(subject, callback);
                }
            }
        } else {
            String subject = serviceImplementation.getSubject();
            logger.info("Subscribing RPC " + subject);
            final Map<String, MessageCallback> epts = new HashMap<String, MessageCallback>();
            final ServiceInstanceProvider serviceInstanceProvider = new ServiceInstanceProvider() {
                @Override
                public Object get(Message message) {
                    return applicationContext.getBean(serviceImplementation.getBeanName());
                }
            };

            Class<?> remoteInterface = serviceTypeParser.getRemoteImplementation();
            for (final Method method : remoteInterface.getMethods()) {
                if (ProxyUtil.isMethodInInterface(remoteInterface, method)) {
                    epts.put(ProxyUtil.createCallSignature(remoteInterface, method),
                            RPCEndpointFactory.createEndpointFor(serviceInstanceProvider, method, bus));
                }
            }

            final RemoteServiceCallback delegate = new RemoteServiceCallback(epts);
            bus.subscribe(subject, new MessageCallback() {
                @Override
                public void callback(final Message message) {
                    delegate.callback(message);
                }
            });

            DefaultRemoteCallBuilder.setProxyFactory(Assert.notNull(new ProxyFactory() {
                @Override
                public <T> T getRemoteProxy(final Class<T> proxyType) {
                    throw new RuntimeException(
                            "There is not yet an available Errai RPC implementation for the server-side environment.");
                }
            }));
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("Look for Errai Service definitions");
        String[] beans = beanFactory.getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> beanType = beanFactory.getType(beanName);
            Service service = AnnotationUtils.findAnnotation(beanType, Service.class);
            if (service != null) {
                try {
                    ServiceTypeParser serviceTypeParser = new ServiceTypeParser(beanType);
                    services.add(new ServiceImplementation(serviceTypeParser, beanName));
                } catch (NotAService e) {
                    logger.warn("Service annotation present but threw NotAServiceException", e);
                }
            }
        }
    }
}