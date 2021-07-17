package org.apache.isis.security.keycloak.handler;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.apache.isis.core.security.authentication.logout.LogoutHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Propagates logouts to Keycloak.
 *
 * <p>
 * Necessary because Spring Security 5 (currently) doesn't support
 * end-session-endpoints.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutHandlerForKeycloak implements LogoutHandler {

    private final RestTemplate restTemplate;

    public LogoutHandlerForKeycloak() {
        this(new RestTemplate());
    }

    @Override public void logout() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            propagateLogoutToKeycloak((OidcUser) authentication.getPrincipal());
        }

    }
    private void propagateLogoutToKeycloak(OidcUser user) {

        val endSessionEndpoint = String.format("%s/protocol/openid-connect/logout", user.getIssuer());

        val builder = UriComponentsBuilder
                .fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());

        val logoutResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
        if (logoutResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully logged out in Keycloak");
        } else {
            log.info("Could not propagate logout to Keycloak");
        }
    }

    @Override public boolean isHandlingCurrentThread() {
        return true;
    }
}
