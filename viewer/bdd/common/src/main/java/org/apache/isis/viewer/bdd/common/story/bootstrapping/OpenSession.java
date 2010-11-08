package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.standard.fixture.AuthenticationRequestLogonFixture;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.viewer.bdd.common.Story;

public class OpenSession {

    @SuppressWarnings("unused")
    private final Story story;

    public OpenSession(final Story story) {
        this.story = story;
    }

    public void openSession(final String userName, final String[] roles) {
        IsisContext.closeSession();
        final LogonFixture logonFixture = new LogonFixture(userName, roles);
        final AuthenticationRequestLogonFixture authRequest = new AuthenticationRequestLogonFixture(
                logonFixture);
        final AuthenticationSession authSession = getAuthenticationManager().authenticate(authRequest);

        IsisContext.openSession(authSession);
    }

    protected AuthenticationManager getAuthenticationManager() {
        return IsisContext.getAuthenticationManager();
    }

}
