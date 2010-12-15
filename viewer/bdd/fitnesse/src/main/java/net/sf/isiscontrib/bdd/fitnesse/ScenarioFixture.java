package net.sf.isiscontrib.bdd.fitnesse;

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

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.Scenario;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer.CheckMode;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

import fit.Fixture;
import fitlibrary.DoFixture;

public class ScenarioFixture extends DoFixture {

    private static ThreadLocal<Scenario> scenarioThreadLocal = new ThreadLocal<Scenario>() {
        @Override
        protected Scenario initialValue() {
            return new Scenario();
        }
    };

    public static Scenario getScenario() {
        return scenarioThreadLocal.get();
    }

    public ScenarioFixture() {
        DateParser dateParser = getScenario().getDateParser();
        registerParseDelegate(java.util.Date.class, dateParser);
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
        getScenario().bootstrapIsis(configDirectory, deploymentType);
    }

    public String getConfigDirectory() {
        return getScenario().getConfigDirectory();
    }

    public DeploymentType getDeploymentType() {
        return getScenario().getDeploymentType();
    }

    public void shutdownIsis() {
        getScenario().shutdownIsis();
    }

    // ////////////////////////////////////////////////////////////
    // Date/Time
    // ////////////////////////////////////////////////////////////

    public void dateIsNow(final String dateStr) {
        dateAndTimeIs(dateStr);
    }

    public void dateIs(final String dateStr) {
        dateAndTimeIs(dateStr);
    }

    public void timeIsNow(final String timeStr) {
        dateAndTimeIs(timeStr);
    }

    public void timeIs(final String timeStr) {
        dateAndTimeIs(timeStr);
    }

    private void dateAndTimeIs(final String dateAndOrTimeStr) {
        getScenario().dateAndTimeIs(dateAndOrTimeStr);
    }

    // ////////////////////////////////////////////////////////////
    // LogonAs / SwitchUser
    // ////////////////////////////////////////////////////////////

    public void logonAs(final String userName) {
        getScenario().logonAsOrSwitchUserTo(userName);
    }

    public void logonAsWithRoles(final String userName, final String roleListStr) {
        List<String> roleList = StringUtils.splitOnCommas(roleListStr);
        getScenario().logonAsOrSwitchUserTo(userName, roleList);
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
        return new AliasServicesForFitNesse(getScenario().getAliasRegistry());
    }

    public void aliasServiceAs(final String alias, final String serviceClassName) {
        try {
            getScenario().getAliasRegistry().aliasService(alias, serviceClassName);
        } catch (ScenarioValueException ex) {
            throw new ScenarioFitNesseException(ex);
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
        return new SetUpObjectsForFitNesse(getScenario().getAliasRegistry(), className, SetUpObjectsPeer.Mode.PERSIST);
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
        return new SetUpObjectsForFitNesse(getScenario().getAliasRegistry(), className,
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
        return new UsingIsisViewerForFitNesse(getScenario().getAliasRegistry(), getScenario().getDeploymentType(), getScenario().getDateParser(), mode);
    }

    // ////////////////////////////////////////////////////////////
    // Check List
    // ////////////////////////////////////////////////////////////

    public Fixture checkListContains(final String listAlias) {
        return new CheckListForFitNesse(getScenario().getAliasRegistry(), listAlias, CheckMode.NOT_EXACT);
    }

    public Fixture checkListPreciselyContains(final String listAlias) {
        return new CheckListForFitNesse(getScenario().getAliasRegistry(), listAlias, CheckMode.EXACT);
    }

    /**
     * @see #checkListPreciselyContains(String)
     */
    public Fixture checkListExactlyContains(final String listAlias) {
        return checkListPreciselyContains(listAlias);
    }

    public Fixture aliasItemsInList(final String listAlias) {
        return new AliasItemsInListForFitNesse(getScenario().getAliasRegistry(), listAlias);
    }

    // ///////////////////////////////////////////////////////
    // run viewer
    // ///////////////////////////////////////////////////////

    public void runViewer() {
        getScenario().runViewer();
    }

    // ///////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////

    public Fixture checkSpecificationsLoaded() {
        return new CheckSpecificationsLoadedForFitNesse(getScenario().getAliasRegistry());
    }

    public Fixture debugServices() {
        return new DebugServicesForFitNesse(getScenario().getAliasRegistry());
    }

    public Fixture debugObjectStore() {
        return new DebugObjectStoreForFitNesse(getScenario().getAliasRegistry());
    }

    public Fixture debugClock() {
        return new DebugClockForFitNesse(getScenario().getAliasRegistry());
    }

}
