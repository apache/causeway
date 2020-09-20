package org.apache.isis.extensions.jaxrsclient.impl.client;

import javax.ws.rs.core.Response;

import org.apache.isis.extensions.jaxrsclient.applib.client.JaxRsResponse;

class JaxRsResponseDefault implements JaxRsResponse {

    private final Response response;

    public JaxRsResponseDefault(final Response response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public <T> T readEntity(final Class<T> entityType) {
        return response.readEntity(entityType);
    }
}
