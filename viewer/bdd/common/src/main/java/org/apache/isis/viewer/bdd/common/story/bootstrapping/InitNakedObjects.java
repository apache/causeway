package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.fixturesinstaller.FixturesInstallerNoop;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtime.runner.IsisModule;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.system.SystemConstants;
import org.apache.isis.runtime.system.internal.InitialisationSession;
import org.apache.isis.runtime.userprofile.inmemory.InMemoryUserProfileStoreInstaller;
import org.apache.isis.viewer.bdd.common.Story;
import org.apache.isis.viewer.bdd.common.components.StoryAuthenticationManagerInstaller;
import org.apache.isis.viewer.bdd.common.components.StoryInMemoryPersistenceMechanismInstaller;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InitNakedObjects extends AbstractHelper {

	private final ConfigurationBuilder configurationBuilder;
	private final DeploymentType deploymentType;

    public InitNakedObjects(final Story story, ConfigurationBuilder configurationBuilder, DeploymentType deploymentType) {
        super(story);
        this.configurationBuilder = configurationBuilder;
        this.deploymentType = deploymentType;
    }

    public void initialize() {
        configurationBuilder.add(SystemConstants.AUTHENTICATION_INSTALLER_KEY,
                StoryAuthenticationManagerInstaller.class.getName());
        configurationBuilder.add(
                SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY,
                StoryInMemoryPersistenceMechanismInstaller.class.getName());
        configurationBuilder.add(
                SystemConstants.PROFILE_PERSISTOR_INSTALLER_KEY,
                InMemoryUserProfileStoreInstaller.class.getName());
        configurationBuilder.add(SystemConstants.FIXTURES_INSTALLER_KEY,
                FixturesInstallerNoop.class.getName());
        configurationBuilder.add(SystemConstants.NOSPLASH_KEY, "" + true);

        IsisSystem system = null;
        try {

            final InstallerLookupDefault installerLookup = new InstallerLookupDefault(
                    getClass());
            configurationBuilder.injectInto(installerLookup);

//            system = new IsisSystemBootstrapper(installerLookup)
//                    .bootSystem(deploymentType);

            configurationBuilder.add(SystemConstants.DEPLOYMENT_TYPE_KEY,
                    deploymentType.name());
            Injector injector = createGuiceInjector(deploymentType, configurationBuilder, installerLookup);
            system = injector.getInstance(IsisSystem.class);
            
            getStory().setInstallerLookup(installerLookup);
            getStory().setNakedObjectsSystem(system);

            // provide a session in order to install fixtures
            IsisContext.openSession(new InitialisationSession());

        } catch (final RuntimeException e) {
            if (system != null) {
                system.shutdown();
            }
            throw e;
        }
    }
    
    private Injector createGuiceInjector(DeploymentType deploymentType,
            ConfigurationBuilder configurationBuilder,
            InstallerLookup installerLookup) {
        IsisModule isisModule = new IsisModule(deploymentType,configurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }


}
