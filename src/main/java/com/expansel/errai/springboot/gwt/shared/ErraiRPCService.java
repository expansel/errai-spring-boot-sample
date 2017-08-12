package com.expansel.errai.springboot.gwt.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

@Remote
public interface ErraiRPCService {

    public RPCResult callPing();

    @RestrictedAccess(roles = { "admin" })
    public void triggerMessage();

}
