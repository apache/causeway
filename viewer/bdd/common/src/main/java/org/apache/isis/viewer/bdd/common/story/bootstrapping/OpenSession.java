package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import java.util.List;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.fixture.AuthenticationRequestLogonFixture;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.bdd.common.Scenario;

public class OpenSession {

    @SuppressWarnings("unused")
    private final Scenario story;

    public OpenSession(final Scenario story) {
        this.story = story;
    }

    public void openSession(final String userName, final List<String> roles) {
        IsisContext.closeSession();
        final LogonFixture logonFixture = new LogonFixture(userName, roles);
        final AuthenticationRequestLogonFixture authRequest = new AuthenticationRequestLogonFixture(logonFixture);
        final AuthenticationSession authSession = getAuthenticationManager().authenticate(authRequest);

        IsisContext.openSession(authSession);
    }

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
