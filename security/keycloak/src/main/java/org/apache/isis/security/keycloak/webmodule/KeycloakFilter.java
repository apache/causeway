package org.apache.isis.security.keycloak.webmodule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.security.api.authentication.standard.SimpleSession;
import org.apache.isis.core.webapp.wormhole.AuthenticationSessionWormhole;

import lombok.val;

public class KeycloakFilter implements Filter {

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        final String userid = header(httpServletRequest, "X-Auth-Userid");
        final String rolesHeader = header(httpServletRequest, "X-Auth-Roles");
        final String subjectHeader = header(httpServletRequest, "X-Auth-Subject");
        if(userid == null || rolesHeader == null || subjectHeader == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final List<String> roles = toClaims(rolesHeader);
        try {
            val authenticationSession = new SimpleSession(userid, roles, subjectHeader);
            authenticationSession.setType(AuthenticationSession.Type.EXTERNAL);
            AuthenticationSessionWormhole.sessionByThread.set(authenticationSession);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AuthenticationSessionWormhole.sessionByThread.remove();
        }
    }

    static List<String> toClaims(final String claimsHeader) {
        final List<String> roles = asRoles(claimsHeader);
        roles.add("org.apache.isis.viewer.wicket.roles.USER");
        return roles;
    }

    static List<String> asRoles(String claimsHeader) {
        final List<String> roles = new ArrayList<>();
        if(claimsHeader != null) {
            roles.addAll(Arrays.asList(claimsHeader.split(",")));
        }
        return roles;
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
