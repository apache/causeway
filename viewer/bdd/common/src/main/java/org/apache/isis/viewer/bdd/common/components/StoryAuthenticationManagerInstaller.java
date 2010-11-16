package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandardInstallerAbstract;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.defaults.security.authentication.AuthenticatorNoop;

public class StoryAuthenticationManagerInstaller extends
        AuthenticationManagerStandardInstallerAbstract {

    public StoryAuthenticationManagerInstaller() {
        super("story");
    }

    @Override
    protected Authenticator createAuthenticator(
            final IsisConfiguration configuration) {
        return new AuthenticatorNoop(configuration);
    }
}