package com.expansel.errai.springboot;

import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.servlet.DefaultBlockingServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.RequestContextFilter;

import com.expansel.errai.spring.server.ErraiRequestDispatcherFactoryBean;
import com.expansel.errai.spring.server.ErraiServerMessageBusFactoryBean;

@Configuration
@ComponentScan({ "com.expansel.errai.springboot", "com.expansel.errai.spring.server",
        "com.expansel.errai.springsecurity.server" })
@EnableAutoConfiguration
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FilterRegistrationBean requestContextFilter() {
        final FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(new RequestContextFilter());
        filterRegBean.addUrlPatterns("*");
        filterRegBean.setEnabled(Boolean.TRUE);
        filterRegBean.setName("RequestContextFilter");
        return filterRegBean;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        logger.info("Registering Errai Servlet");
        ServletRegistrationBean registration = new ServletRegistrationBean(new DefaultBlockingServlet(), "*.erraiBus");
        registration.addInitParameter("auto-discover-services", "false");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public FactoryBean<ServerMessageBus> erraiServerMessageBusFactoryBean() {
        logger.info("Creating Errai ServiceMessageBus FactoryBean");
        return new ErraiServerMessageBusFactoryBean();
    }

    @Bean
    public FactoryBean<RequestDispatcher> erraiRequestDispatcherFactoryBean() {
        logger.info("Creating Errai RequestDispatcher FactoryBean");
        return new ErraiRequestDispatcherFactoryBean();
    }

}
