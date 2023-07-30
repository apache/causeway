package org.apache.causeway.viewer.restfulobjects.client.auth.basic;

import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.auth.AuthorizationHeaderFactory;

public class AuthorizationHeaderFactoryBasic implements AuthorizationHeaderFactory {

    private final BasicCreds creds;

    public AuthorizationHeaderFactoryBasic(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getAuthenticationMode() != AuthenticationMode.BASIC) {
            throw new IllegalArgumentException(String.format("config.authenticationMode must be '%s'", AuthenticationMode.BASIC));
        }
        this.creds = basicCredsFrom(restfulClientConfig);
    }

    static BasicCreds basicCredsFrom(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getBasicAuthUser() == null) {
            throw new IllegalArgumentException("config.basicAuthUser must be set");
        }
        if (restfulClientConfig.getBasicAuthPassword() == null) {
            throw new IllegalArgumentException("config.basicAuthPassword must be set");
        }
        return BasicCreds.builder()
                .username(restfulClientConfig.getBasicAuthUser())
                .password(restfulClientConfig.getBasicAuthPassword())
                .build();
    }


    @Override
    public String create() {
        return "Basic " + encode(creds.getUsername(), creds.getPassword());
    }

    static String encode(final String username, final String password) {
        return java.util.Base64.getEncoder().encodeToString(asBytes(username, password));
    }

    static byte[] asBytes(final String username, final String password) {
        return String.format("%s:%s", username, password).getBytes();
    }


}
