package org.apache.isis.extensions.bdd.fitnesse;

import java.util.Date;

import org.apache.isis.extensions.bdd.common.Story;
import org.apache.isis.extensions.bdd.common.StoryValueException;
import org.apache.isis.extensions.bdd.common.fixtures.SetUpObjectsPeer;
import org.apache.isis.extensions.bdd.common.fixtures.CheckListPeer.CheckMode;
import org.apache.isis.extensions.bdd.common.fixtures.perform.Perform;
import org.apache.isis.extensions.bdd.common.parsers.JavaUtilDateParser;
import org.apache.isis.extensions.bdd.common.story.bootstrapping.InitNakedObjects;
import org.apache.isis.extensions.bdd.fitnesse.internal.FitnesseConfigurationBuilder;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.AliasItemsInListForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.AliasServicesForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.CheckListForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.CheckSpecificationsLoadedForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.DebugClockForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.DebugObjectStoreForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.DebugServicesForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.SetUpObjectsForFitNesse;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.UsingNakedObjectsViewerForFitNesse;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;

import fit.Fixture;
import fitlibrary.DoFixture;

public class StoryFixture extends DoFixture {

	private static ThreadLocal<Story> storyThreadLocal = new ThreadLocal<Story>() {
		@Override
		protected Story initialValue() {
			return new Story();
		}
	};
	
	public static Story getStory() {
		return storyThreadLocal.get();
	}

    public StoryFixture() {
        registerParseDelegate(java.util.Date.class, new JavaUtilDateParser());
    }

    public void initNakedObjects() {
        InitNakedObjects initializer = new InitNakedObjects(getStory(), newConfigurationBuilder(), getDeploymentType());
		initializer.initialize();
    }

    public void logonAs(final String userName) {
        getStory().logonAs(userName);
    }

    public void logonAsWithRoles(final String userName, final String roleList) {
    	getStory().logonAsWithRoles(userName, roleList);
    }

    /**
     * Switch user, specifying no roles.
     */
    public void switchUser(final String userName) {
    	getStory().switchUser(userName);
    }

    /**
     * Switch user, specifying roles.
     */
    public void switchUserWithRoles(final String userName, final String roleList) {
    	getStory().switchUserWithRoles(userName, roleList);
    }

    public Fixture aliasServices() {
        return new AliasServicesForFitNesse(getStory());
    }

    
    public void dateIsNow(final Date dateAndTime) {
        getStory().dateIs(dateAndTime);
    }

    public void dateIs(final Date dateAndTime) {
    	getStory().timeIs(dateAndTime);
    }

    public void timeIsNow(final Date dateAndTime) {
    	getStory().timeIs(dateAndTime);
    }

    public void timeIs(final Date dateAndTime) {
    	getStory().timeIs(dateAndTime);
    }

    public void shutdownNakedObjects() {
        getStory().shutdownNakedObjects();
    }

    /**
     * Allow for singular form.
     * 
     * @see #setUpObjects(String)
     */
    public Fixture setUpObject(final String className) {
        return setUpObjects(className);
    }

    public Fixture setUpObjects(final String className) {
        return new SetUpObjectsForFitNesse(getStory(), className, SetUpObjectsPeer.Mode.PERSIST);
    }

    /**
     * Allow for singular form.
     * 
     * @see #setUpTransientObjects(String)
     */
    public Fixture setUpTransientObject(final String className) {
        return setUpTransientObjects(className);
    }

    public Fixture setUpTransientObjects(final String className) {
        return new SetUpObjectsForFitNesse(getStory(), className, 
                SetUpObjectsPeer.Mode.DO_NOT_PERSIST);
    }

    public Fixture usingNakedObjectsViewer() {
        return usingNakedObjectsViewer(Perform.Mode.TEST);
    }

    /**
     * Allow for mis-spellings.
     */
    public Fixture usingNakedObjectsViewerForSetup() {
        return usingNakedObjectsViewerForSetUp();
    }

    public Fixture usingNakedObjectsViewerForSetUp() {
        return usingNakedObjectsViewer(Perform.Mode.SETUP);
    }

    public Fixture usingNakedObjectsViewer(final Perform.Mode mode) {
        return new UsingNakedObjectsViewerForFitNesse(getStory(), mode);
    }

    public Fixture checkListContains(final String listAlias) {
        return new CheckListForFitNesse(getStory(), listAlias, CheckMode.NOT_EXACT);
    }

    public Fixture checkListPreciselyContains(final String listAlias) {
        return new CheckListForFitNesse(getStory(), listAlias, CheckMode.EXACT);
    }

    /**
     * @see #checkListPreciselyContains(String)
     */
    public Fixture checkListExactlyContains(final String listAlias) {
        return checkListPreciselyContains(listAlias);
    }

    public Fixture aliasItemsInList(final String listAlias) {
        return new AliasItemsInListForFitNesse(getStory(), listAlias);
    }

    // ///////////////////////////////////////////////////////
    // StoryDriver (delegates)
    // ///////////////////////////////////////////////////////

    public void runViewer() {
        getStory().runViewer();
    }


    public String getConfigDirectory() {
        return getStory().getConfigDirectory();
    }
    public void setConfigDirectory(final String configDirectory) {
        getStory().setConfigDirectory(configDirectory);
    }


    public void enableExploration() {
        getStory().enableExploration();
    }

    public DeploymentType getDeploymentType() {
        return getStory().getDeploymentType();
    }

    // ///////////////////////////////////////////////////////
    // Diagnostics
    // ///////////////////////////////////////////////////////

    public Fixture checkSpecificationsLoaded() {
        return new CheckSpecificationsLoadedForFitNesse(getStory());
    }

    public Fixture debugServices() {
        return new DebugServicesForFitNesse(getStory());
    }

    public Fixture debugObjectStore() {
        return new DebugObjectStoreForFitNesse(getStory());
    }

    public Fixture debugClock() {
        return new DebugClockForFitNesse(getStory());
    }

    public void aliasServiceAs(final String alias, final String serviceClassName) {
    	try {
			getStory().registerService(alias, serviceClassName);
		} catch (StoryValueException ex) {
			throw new StoryFitNesseException(ex);
		}
    }

    
    // /////////////////////////////////////////////////////////
    // System
    // /////////////////////////////////////////////////////////

    public IsisSystem getSystem() {
        return getStory().getSystem();
    }

    public void setNakedObjectsSystem(
            final IsisSystem nakedObjectsSystem) {
        getStory().setNakedObjectsSystem(nakedObjectsSystem);
    }

    public InstallerLookup getInstallerLookup() {
        return getStory().getInstallerLookup();
    }

    public void setInstallerLookup(final InstallerLookup installerLookup) {
        getStory().setInstallerLookup(installerLookup);
    }

    public ConfigurationBuilder newConfigurationBuilder() {
    	return new FitnesseConfigurationBuilder(
    			getConfigDirectory());
    }
    
    
}
