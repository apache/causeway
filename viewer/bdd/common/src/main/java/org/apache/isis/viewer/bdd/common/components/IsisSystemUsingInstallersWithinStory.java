package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.installers.InstallerLookup;
import org.apache.isis.core.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.installers.IsisSystemUsingInstallers;
import org.apache.isis.core.runtime.userprofile.UserProfileStoreInstaller;
import org.apache.isis.defaults.profilestore.InMemoryUserProfileStoreInstaller;

public class IsisSystemUsingInstallersWithinStory extends IsisSystemUsingInstallers {

    public IsisSystemUsingInstallersWithinStory(final DeploymentType deploymentType,
        final InstallerLookup installerLookup) {
        super(deploymentType, installerLookup);

        final AuthenticationManagerInstaller authManagerInstaller = new StoryAuthenticationManagerInstaller();
        setAuthenticationInstaller(getInstallerLookup().injectDependenciesInto(authManagerInstaller));

        final PersistenceMechanismInstaller persistorInstaller = new StoryInMemoryPersistenceMechanismInstaller();
        setPersistenceMechanismInstaller(getInstallerLookup().injectDependenciesInto(persistorInstaller));

        final UserProfileStoreInstaller userProfileStoreInstaller = new InMemoryUserProfileStoreInstaller();
        setUserProfileStoreInstaller(getInstallerLookup().injectDependenciesInto(userProfileStoreInstaller));
    }

}
