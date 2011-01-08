package org.apache.isis.viewer.restful.viewer.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.standard.exploration.AuthenticationRequestExploration;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.webapp.auth.AuthenticationSessionLookupStrategyDefault;

public class AuthenticationSessionLookupStrategyTrusted extends AuthenticationSessionLookupStrategyDefault {

    @Override
    public AuthenticationSession lookup(ServletRequest servletRequest, ServletResponse servletResponse) {
        AuthenticationSession session = super.lookup(servletRequest, servletResponse);
        if (session != null) {
            return session;
        }

        // will always succeed.
        AuthenticationRequestExploration request = new AuthenticationRequestExploration();
        return IsisContext.getAuthenticationManager().authenticate(request);
    }
}
