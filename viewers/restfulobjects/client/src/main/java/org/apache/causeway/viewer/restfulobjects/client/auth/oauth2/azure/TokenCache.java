package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

import java.time.temporal.ChronoUnit;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.causeway.commons.functional.Railway;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.Oauth2Creds;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.val;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class TokenCache {

    protected static Logger LOG = LoggerFactory.getLogger(TokenCache.class);

    private final RetryPolicy<Response> retryPolicyForAnyException = retryPolicyForAnyException();

    private static <T> RetryPolicy<T> retryPolicyForAnyException() {
        return retryPolicyFor(Exception.class);
    }

    private static <T> RetryPolicy<T> retryPolicyFor(Class<? extends Exception>... exceptionClasses) {
        return new RetryPolicy<T>()
                .handle(exceptionClasses)
                .withBackoff(500, 16000, ChronoUnit.MILLIS)
                .withMaxRetries(3)
                .onRetry(ctx -> {
                    LOG.warn("Failed attempt {}, retrying...", (ctx.getAttemptCount() - 1));
                })
                .onFailure(ctx -> LOG.warn("Failed retry policy after {} attempts. Exception:\n {}", (ctx.getAttemptCount() - 1), _Exceptions.asStacktrace(
                        ctx.getFailure())));
    }

    private final Oauth2Creds creds;
    private final ClientBuilder clientBuilder;

    public TokenCache(final Oauth2Creds creds) {
        this.creds = creds;
        this.clientBuilder = ClientBuilder.newBuilder();
    }

    private String jwtToken;
    private DateTime jwtTokenExpiresAt;

    public Railway<Exception, String> getToken() {

        if (isTokenValid()) {
            return Railway.success(jwtToken);
        }

        val client = clientBuilder.build();
        val webTarget = client.target(UriBuilder.fromUri(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", creds.getTenantId())));
        val invocationBuilder = webTarget.request()
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .header(HttpHeaders.ACCEPT, "application/json")
                ;
        val form = new Form().param("scope", String.format("%s/.default", creds.getClientId()))
                .param("client_id", creds.getClientId())
                .param("client_secret", creds.getClientSecret())
                .param("grant_type", "client_credentials");

        val invocation = invocationBuilder.buildPost(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        val response = Failsafe.with(retryPolicyForAnyException).get(() -> invocation.invoke());

        val entity = response.readEntity(String.class);

        if (response.getStatus() != 200) {

            this.jwtToken = null;
            this.jwtTokenExpiresAt = null;

            return Railway.failure(new RuntimeException(entity));
        }

        val result = new TokenParser().parseTokenEntity(entity);
        if (result.isFailure()) {
            this.jwtToken = null;
            this.jwtTokenExpiresAt = null;

            return Railway.failure(result.getFailureElseFail());
        }

        val tsr = result.getSuccessElseFail();

        int expiresIn = tsr.getExpires_in();
        this.jwtToken = tsr.getAccess_token();
        this.jwtTokenExpiresAt = now().plusMinutes(expiresIn);

        return Railway.success(jwtToken);
    }

    public boolean isTokenExpired() {
        return !isTokenValid();
    }

    private boolean isTokenValid() {
        if (jwtTokenExpiresAt == null) {
            return false;
        }

        // token must remain valid for at least 5 minutes from now.
        val inFiveMinutesTime = now().plusMinutes(5);
        return jwtTokenExpiresAt.isBefore(inFiveMinutesTime);
    }

    private static DateTime now() {
        return DateTime.now();
    }

}
