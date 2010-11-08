package org.apache.isis.viewer.restful.viewer.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.authentication.standard.exploration.AuthenticationRequestExploration;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.auth.AuthenticationSessionLookupStrategyDefault;

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
