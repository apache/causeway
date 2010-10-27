package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.runtime.authentication.standard.AuthenticationManagerStandardInstallerAbstract;
import org.apache.isis.runtime.authentication.standard.Authenticator;
import org.apache.isis.runtime.authentication.standard.noop.AuthenticatorNoop;

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