package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandardInstallerAbstract;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.runtimes.dflt.runtime.authentication.AuthenticationManagerStandardInstallerAbstractForDfltRuntime;
import org.apache.isis.security.dflt.authentication.AuthenticatorNoop;

public class BddAuthenticationManagerInstaller extends
        AuthenticationManagerStandardInstallerAbstractForDfltRuntime {

    public BddAuthenticationManagerInstaller() {
        super("bdd");
    }

    @Override
    protected Authenticator createAuthenticator(
            final IsisConfiguration configuration) {
        return new AuthenticatorNoop(configuration);
    }
}