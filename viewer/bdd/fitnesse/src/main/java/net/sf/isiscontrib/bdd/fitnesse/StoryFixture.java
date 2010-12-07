package net.sf.isiscontrib.bdd.fitnesse;

import java.util.Date;
import java.util.List;

import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.AliasItemsInListForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.AliasServicesForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.CheckListForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.CheckSpecificationsLoadedForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.DebugClockForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.DebugObjectStoreForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.DebugServicesForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.SetUpObjectsForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.UsingIsisViewerForFitNesse;

import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.Story;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer.CheckMode;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.parsers.JavaUtilDateParser;
import org.apache.isis.viewer.bdd.common.util.Strings;

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

    // ////////////////////////////////////////////////////////////
    // Bootstrap / shutdown
    // ////////////////////////////////////////////////////////////

    /**
     * Bootstrapping
     * 
     * @param configDirectory
     * @param deploymentTypeStr
     */
    public void bootstrapIsisConfiguredFromInMode(String configDirectory, String deploymentTypeStr) {
        DeploymentType deploymentType = DeploymentType.lookup(deploymentTypeStr);
        getStory().bootstrapIsis(configDirectory, deploymentType);
    }

    public String getConfigDirectory() {
        return getStory().getConfigDirectory();
    }

    public DeploymentType getDeploymentType() {
        return getStory().getDeploymentType();
    }

    public void shutdownIsis() {
        getStory().shutdownIsis();
    }

    // ////////////////////////////////////////////////////////////
    // Date/Time
    // ////////////////////////////////////////////////////////////

    public void dateIsNow(final Date dateAndTime) {
        dateAndTimeIs(dateAndTime);
    }

    public void dateIs(final Date dateAndTime) {
        dateAndTimeIs(dateAndTime);
    }

    public void timeIsNow(final Date dateAndTime) {
        dateAndTimeIs(dateAndTime);
    }

    public void timeIs(final Date dateAndTime) {
        dateAndTimeIs(dateAndTime);
    }

    private void dateAndTimeIs(final Date dateAndTime) {
        getStory().dateAndTimeIs(dateAndTime);
    }

    // ////////////////////////////////////////////////////////////
    // LogonAs / SwitchUser
    // ////////////////////////////////////////////////////////////

    public void logonAs(final String userName) {
        getStory().logonAsOrSwitchUserTo(userName);
    }

    public void logonAsWithRoles(final String userName, final String roleListStr) {
        List<String> roleList = Strings.splitOnCommas(roleListStr);
        getStory().logonAsOrSwitchUserTo(userName, roleList);
    }

    /**
     * Switch user, specifying no roles.
     */
    public void switchUser(final String userName) {
        logonAs(userName);
    }

    /**
     * Switch user, specifying roles.
     */
    public void switchUserWithRoles(final String userName, final String roleList) {
        logonAsWithRoles(userName, roleList);
    }

    // ////////////////////////////////////////////////////////////
    // Alias Services
    // ////////////////////////////////////////////////////////////

    public Fixture aliasServices() {
        return new AliasServicesForFitNesse(getStory().getAliasRegistry());
    }

    public void aliasServiceAs(final String alias, final String serviceClassName) {
        try {
            getStory().getAliasRegistry().aliasService(alias, serviceClassName);
        } catch (StoryValueException ex) {
            throw new StoryFitNesseException(ex);
        }
    }

    // ////////////////////////////////////////////////////////////
    // setUp story
    // ////////////////////////////////////////////////////////////

    /**
     * Allow for singular form.
     * 
     * @see #setUpObjects(String)
     */
    public Fixture setUpObject(final String className) {
        return setUpObjects(className);
    }

    public Fixture setUpObjects(final String className) {
        return new SetUpObjectsForFitNesse(getStory().getAliasRegistry(), className, SetUpObjectsPeer.Mode.PERSIST);
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
        return new SetUpObjectsForFitNesse(getStory().getAliasRegistry(), className,
            SetUpObjectsPeer.Mode.DO_NOT_PERSIST);
    }

    // ////////////////////////////////////////////////////////////
    // Isis Viewer
    // ////////////////////////////////////////////////////////////

    public Fixture usingIsisViewer() {
        return usingIsisViewer(Perform.Mode.TEST);
    }

    /**
     * Allow for mis-spellings.
     */
    public Fixture usingIsisViewerForSetup() {
        return usingIsisViewerForSetUp();
    }

    public Fixture usingIsisViewerForSetUp() {
        return usingIsisViewer(Perform.Mode.SETUP);
    }

    private Fixture usingIsisViewer(final Perform.Mode mode) {
        return new UsingIsisViewerForFitNesse(getStory().getAliasRegistry(), getStory().getDateParser(), mode);
    }

    // ////////////////////////////////////////////////////////////
    // Check List
    // ////////////////////////////////////////////////////////////

    public Fixture checkListContains(final String listAlias) {
        return new CheckListForFitNesse(getStory().getAliasRegistry(), listAlias, CheckMode.NOT_EXACT);
    }

    public Fixture checkListPreciselyContains(final String listAlias) {
        return new CheckListForFitNesse(getStory().getAliasRegistry(), listAlias, CheckMode.EXACT);
    }

    /**
     * @see #checkListPreciselyContains(String)
     */
    public Fixture checkListExactlyContains(final String listAlias) {
        return checkListPreciselyContains(listAlias);
    }

    public Fixture aliasItemsInList(final String listAlias) {
        return new AliasItemsInListForFitNesse(getStory().getAliasRegistry(), listAlias);
    }

    // ///////////////////////////////////////////////////////
    // run viewer
    // ///////////////////////////////////////////////////////

    public void runViewer() {
        getStory().runViewer();
    }

    // ///////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////

    public Fixture checkSpecificationsLoaded() {
        return new CheckSpecificationsLoadedForFitNesse(getStory().getAliasRegistry());
    }

    public Fixture debugServices() {
        return new DebugServicesForFitNesse(getStory().getAliasRegistry());
    }

    public Fixture debugObjectStore() {
        return new DebugObjectStoreForFitNesse(getStory().getAliasRegistry());
    }

    public Fixture debugClock() {
        return new DebugClockForFitNesse(getStory().getAliasRegistry());
    }

}
