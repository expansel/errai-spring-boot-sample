package com.expansel.errai.springboot.gwt.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class RPCResult {
    private String result;

    public RPCResult() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
