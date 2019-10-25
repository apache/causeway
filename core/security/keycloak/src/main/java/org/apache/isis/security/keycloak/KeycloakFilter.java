package org.apache.isis.security.keycloak;

import lombok.val;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.core.runtime.web.AuthenticationSessionWormhole;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.standard.SimpleSession;

public class KeycloakFilter implements Filter {

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final String userid = header(httpServletRequest, "X-Auth-Userid");
        if(userid == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            val authenticationSession = new SimpleSession(userid, Collections.singletonList("org.apache.isis.viewer.wicket.roles.USER"));
            authenticationSession.setType(AuthenticationSession.Type.EXTERNAL);
            AuthenticationSessionWormhole.sessionByThread.set(authenticationSession);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AuthenticationSessionWormhole.sessionByThread.remove();
        }
    }

    private String header(final HttpServletRequest httpServletRequest, final String headerName) {
        final Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            final String header = headerNames.nextElement();
            if(header.toLowerCase().equals(headerName.toLowerCase())) {
                return httpServletRequest.getHeader(header);
            }
        }
        return null;
    }
}
