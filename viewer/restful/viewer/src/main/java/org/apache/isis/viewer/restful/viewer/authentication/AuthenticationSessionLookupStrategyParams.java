package org.apache.isis.viewer.restful.viewer.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.webapp.auth.AuthenticationSessionLookupStrategyDefault;

public class AuthenticationSessionLookupStrategyParams extends AuthenticationSessionLookupStrategyDefault {

    @Override
    public AuthenticationSession lookup(ServletRequest servletRequest, ServletResponse servletResponse) {
        AuthenticationSession session = super.lookup(servletRequest, servletResponse);
        if (session != null) {
            return session;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String user = httpServletRequest.getParameter("user");
        String password = httpServletRequest.getParameter("password");

        if (user == null || password == null) {
            return null;
        }
        AuthenticationRequestPassword request = new AuthenticationRequestPassword(user, password);
        return IsisContext.getAuthenticationManager().authenticate(request);
    }
}
