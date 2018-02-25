package com.expansel.errai.springboot;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAuthenticationProvider {

    public Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
