package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

import lombok.SneakyThrows;
import lombok.val;

import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.client.auth.AuthorizationHeaderFactory;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.Oauth2Creds;

public class AuthorizationHeaderFactoryOauth2Azure implements AuthorizationHeaderFactory {

    private final TokenCache tokenCache;

    public AuthorizationHeaderFactoryOauth2Azure(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getAuthenticationMode() != AuthenticationMode.OAUTH2_AZURE) {
            throw new IllegalArgumentException(String.format("config.authenticationMode must be '%s'", AuthenticationMode.OAUTH2_AZURE));
        }
        final var oauthCreds = oauthCredsFrom(restfulClientConfig);
        tokenCache = new TokenCache(oauthCreds);
    }

    private static Oauth2Creds oauthCredsFrom(final RestfulClientConfig restfulClientConfig) {
        if (restfulClientConfig.getOauthTenantId() == null) {
            throw new IllegalArgumentException("config.oauthTenantId must be set");
        }
        if (restfulClientConfig.getOauthClientId() == null) {
            throw new IllegalArgumentException("config.oauthClientId must be set");
        }
        if (restfulClientConfig.getOauthClientSecret() == null) {
            throw new IllegalArgumentException("config.oauthClientSecret must be set");
        }
        return Oauth2Creds.builder()
                .tenantId(restfulClientConfig.getOauthTenantId())
                .clientId(restfulClientConfig.getOauthClientId())
                .clientSecret(restfulClientConfig.getOauthClientSecret())
                .build();
    }

    @SneakyThrows
    @Override
    public String create() {
        val tokenResult = tokenCache.getToken();
        if (tokenResult.isFailure()) {
            // TODO: this will cause the invocation to fail; but should we fail more permanently somehow if a JWT token could not be obtained?
            throw tokenResult.getFailureElseFail();
        }
        val token = tokenResult.getSuccessElseFail();
        return "Bearer " + token;
    }
}
