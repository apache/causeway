package org.apache.causeway.viewer.restfulobjects.client.auth;

import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.auth.basic.AuthorizationHeaderFactoryBasic;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure.AuthorizationHeaderFactoryOauth2Azure;

import lombok.val;


public interface AuthorizationHeaderFactory {

    static AuthorizationHeaderFactory factoryFor(
            final RestfulClientConfig restfulClientConfig) {
        val authenticationMode = restfulClientConfig.getAuthenticationMode();
        if (authenticationMode == null) {
            throw new IllegalArgumentException("config.authenticationMode must be set");
        }

        switch (authenticationMode) {
            case BASIC:
                return new AuthorizationHeaderFactoryBasic(restfulClientConfig);
            case OAUTH2_AZURE:
                return new AuthorizationHeaderFactoryOauth2Azure(restfulClientConfig);
            default:
                throw new IllegalArgumentException(String.format("unknown authenticationMode '%s'", authenticationMode));
        }
    }


    String create();
}
