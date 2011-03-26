package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.installers.IsisSystemUsingInstallers;
import org.apache.isis.runtimes.dflt.runtime.userprofile.UserProfileStoreInstaller;
import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.runtimes.dflt.profilestores.dflt.InMemoryUserProfileStoreInstaller;

public class IsisSystemUsingInstallersWithinStory extends IsisSystemUsingInstallers {

    public IsisSystemUsingInstallersWithinStory(final DeploymentType deploymentType,
        final InstallerLookup installerLookup) {
        super(deploymentType, installerLookup);

        final AuthenticationManagerInstaller authManagerInstaller = new BddAuthenticationManagerInstaller();
        setAuthenticationInstaller(getInstallerLookup().injectDependenciesInto(authManagerInstaller));

        final PersistenceMechanismInstaller persistorInstaller = new BddInMemoryPersistenceMechanismInstaller();
        setPersistenceMechanismInstaller(getInstallerLookup().injectDependenciesInto(persistorInstaller));

        final UserProfileStoreInstaller userProfileStoreInstaller = new InMemoryUserProfileStoreInstaller();
        setUserProfileStoreInstaller(getInstallerLookup().injectDependenciesInto(userProfileStoreInstaller));
    }

}
