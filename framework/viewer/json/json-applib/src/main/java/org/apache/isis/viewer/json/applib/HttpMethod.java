package org.apache.isis.viewer.json.applib;

public enum HttpMethod {
    
    GET(javax.ws.rs.HttpMethod.GET),
    PUT(javax.ws.rs.HttpMethod.PUT),
    POST(javax.ws.rs.HttpMethod.POST),
    DELETE(javax.ws.rs.HttpMethod.DELETE);

    private final String javaxRsMethod;
    private HttpMethod(String str) {
        this.javaxRsMethod = str;
        
    }
    public String getJavaxRsMethod() {
        return javaxRsMethod;
    }

}
