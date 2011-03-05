package org.apache.isis.viewer.bdd.concordion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.lang.IoUtils;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.Scenario;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer.Mode;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.concordion.internal.concordion.IsisExecuteCommandWithHeader;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.AliasItemsInListForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.CheckCollectionContentsForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.CheckListForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.SetUpObjectsForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.UsingIsisViewerForConcordion;
import org.concordion.Concordion;
import org.concordion.api.ResultSummary;
import org.concordion.internal.ConcordionBuilder;
import org.concordion.internal.FileTarget;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class AbstractIsisConcordionScenario {

    public static final String DEFAULT_CONCORDION_CSS = "concordion.css";
    
    /**
     * The system property that is searched for to use as the {@link #outputDir() target directory}.
     */
    public static final String DEFAULT_CONCORDION_OUTPUT_DIR_PROPERTY = "concordion.output.dir";
    
    /**
     * The directory used by default if the {@link #DEFAULT_CONCORDION_OUTPUT_DIR_PROPERTY default property}
     * for the {@link #outputDir() target directory} is not specified (and the {@link #outputDir()} method
     * has not been overridden). 
     */
    public static final String DEFAULT_OUTPUT_DIR = "/tmp/concordion";

    public static final String NS_URI = "http://isis.apache.org/2010/concordion";
    private static final String CMD_EXECUTE = "execute";
    
    private static ThreadLocal<Scenario> scenarioThreadLocal = new ThreadLocal<Scenario>() {
        @Override
        protected Scenario initialValue() {
            return new Scenario();
        }
    };

    public static Scenario getScenario() {
        return scenarioThreadLocal.get();
    }

    // ////////////////////////////////////////////////////////////////////////
    // @Test
    // ////////////////////////////////////////////////////////////////////////

    @Test
    public void runScenario() throws Throwable {
        try {
            Concordion concordion = createConcordion();
            ResultSummary resultSummary = concordion.process(this);
            resultSummary.print(System.out, this);
            resultSummary.assertIsSatisfied(this);
        } finally {
            copyCustomCssIfDefined();
        }
    }

    private void copyCustomCssIfDefined() {
        Class<?> cssClass = customCssPackage();
        String customCss = customCss();
        if (cssClass == null || customCss == null) {
            return;
        }
        InputStream cssInputFile = cssClass.getResourceAsStream(customCss);
        String cssPackageName = cssClass.getPackage().getName();
        String cssPackagePath = asPath(cssPackageName);
        String cssOutputFileName = StringUtils.combinePaths(outputDir(), cssPackagePath, customCss);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IoUtils.copy(cssInputFile, baos);
            if (baos.size() > 0) {
                IoUtils.copy(new ByteArrayInputStream(baos.toByteArray()), new FileOutputStream(cssOutputFileName));
            }
        } catch (IllegalArgumentException e) {
            System.err.printf("failed to copy custom CSS to '%s'\n", customCss, cssOutputFileName);
            return;
        } catch (IOException e) {
            System.err.printf("failed to copy custom CSS '%s' to '%s'\n", customCss, cssOutputFileName);
            return;
        }
    }

    private String asPath(String name) {
        return name.replace('.', File.separatorChar);
    }

    // ////////////////////////////////////////////////////////////////////////
    // Hooks
    // ////////////////////////////////////////////////////////////////////////

    /**
     * The directory to which the processed HTML should be copied.
     * 
     * <p>
     * Defaults to the value of the {@value #DEFAULT_CONCORDION_OUTPUT_DIR_PROPERTY} system property,
     * or {@value #DEFAULT_OUTPUT_DIR} if that property is not specified.
     * 
     * <p>
     * Can either be overridden if wish to specify some other mechanism for determining where
     * the output is generated.
     */
    protected String outputDir() {
        String concordionOutputDir = System
                .getProperty(DEFAULT_CONCORDION_OUTPUT_DIR_PROPERTY);
        return StringUtils.isNullOrEmpty(concordionOutputDir)  ? DEFAULT_OUTPUT_DIR
                : concordionOutputDir;
    }

    /**
     * Optional hook method to specify the class (any class) that resides in the same package as the
     * {@link #customCss()}.
     * 
     * <p>
     * Return <tt>null</tt> if no custom CSS has been provided.
     * 
     * @see #customCss()
     */
    protected Class<?> customCssPackage() {
        return null;
    }

    /**
     * Optional hook method to specify the name of the custom CSS file.
     * 
     * <p>
     * Defaults to {@value #DEFAULT_CONCORDION_CSS} so you do not need to override if your custom CSS file has this
     * name. However, it is necessary to override {@link #customCssPackage()} to indicate the package that the CSS
     * resides in.
     * 
     * @see #customCssPackage()
     */
    protected String customCss() {
        return DEFAULT_CONCORDION_CSS;
    }

    private Concordion createConcordion() {
        String targetDir = outputDir();
        if (targetDir == null) {
            throw new IllegalStateException("targetDir() cannot be null");
        }
        ConcordionBuilder builder =
            new ConcordionBuilder(){
        }.withTarget(
                new FileTarget(new File(targetDir)))
                .withCommand(NS_URI, CMD_EXECUTE, new IsisExecuteCommandWithHeader());
        return builder.build();
    }

    // ////////////////////////////////////////////////////////////////////////
    // bootstrapIsis / shutdownIsis
    // ////////////////////////////////////////////////////////////////////////

    /**
     * For calling within a <tt>#setUp()</tt> method.
     * 
     * @see {@link #bootstrapIsis(String, String)}
     */
    public void bootstrapIsis(String configDirectory, DeploymentType deploymentType) {
        getScenario().bootstrapIsis(StringUtils.normalized(configDirectory), deploymentType);
    }

    /**
     * For calling from XHTML script.
     * 
     * @see #bootstrapIsis(String, DeploymentType)
     * @return <tt>boolean</tt> so that XHTML can assert on it.
     */
    public boolean bootstrapIsis(String configDirectory, String deploymentTypeStr) {
        bootstrapIsis(configDirectory, DeploymentType.lookup(StringUtils.normalized(deploymentTypeStr)));
        return true; // any runtime exception will propagate
    }

    public void shutdownIsis() {
        getScenario().shutdownIsis();
    }

    // ////////////////////////////////////////////////////////////////////////
    // logon as / switch user
    // ////////////////////////////////////////////////////////////////////////

    public boolean logonAs(String userName) {
        getScenario().logonAsOrSwitchUserTo(StringUtils.normalized(userName));
        return true;
    }

    public boolean logonAsWithRoles(String userName, String roleListStr) {
        List<String> roleList = StringUtils.splitOnCommas(StringUtils.normalized(roleListStr));
        getScenario().logonAsOrSwitchUserTo(userName, roleList);
        return true;
    }

    public void switchUser(String userName) {
        logonAs(userName);
    }

    public void switchUserWithRoles(String userName, String roleListStr) {
        logonAsWithRoles(userName, roleListStr);
    }

    // ////////////////////////////////////////////////////////////////////////
    // date is / time is
    // ////////////////////////////////////////////////////////////////////////

    public boolean usingDateFormat(String dateFormatStr)  {
        getScenario().usingDateFormat(dateFormatStr);
        return true;
    }

    public boolean usingTimeFormat(String timeFormatStr)  {
        getScenario().usingTimeFormat(timeFormatStr);
        return true;
    }

    public boolean dateIs(String dateAndTimeStr) throws ScenarioValueException {
        return dateAndTimeIs(dateAndTimeStr);
    }

    public boolean timeIs(String dateAndTimeStr) throws ScenarioValueException {
        return dateAndTimeIs(dateAndTimeStr);
    }

    private boolean dateAndTimeIs(String dateAndTimeStr) throws ScenarioValueException {
        return getScenario().dateAndTimeIs(dateAndTimeStr);
    }

    // ////////////////////////////////////////////////////////////////////////
    // alias service
    // ////////////////////////////////////////////////////////////////////////

    public boolean aliasService(String aliasAs, String className) {
        try {
            getScenario().getAliasRegistry().aliasService(StringUtils.normalized(aliasAs), StringUtils.normalized(className));
            return true;
        } catch (ScenarioValueException e) {
            return false;
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // setup object
    // ////////////////////////////////////////////////////////////////////////

    public String setUpObject(String className, String alias, String arg0) {
        return setUpObjectsVarargs(className, alias, arg0);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1) {
        return setUpObjectsVarargs(className, alias, arg0, arg1);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4, String arg5) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4, String arg5, String arg6) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4, String arg5, String arg6, String arg7) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4, String arg5, String arg6, String arg7, String arg8) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }

    public String setUpObject(String className, String alias, String arg0, String arg1, String arg2, String arg3,
        String arg4, String arg5, String arg6, String arg7, String arg8, String arg9) {
        return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }

    private SetUpObjectsForConcordion setUpObjects;

    /**
     * Workaround for OGNL defect.
     */
    protected String setUpObjectsVarargs(String className, String alias, String... propertyValues) {
        return setUpObjectsVarargsNormalized(StringUtils.normalized(className), StringUtils.normalized(alias), StringUtils.normalized(propertyValues));
    }

    private String setUpObjectsVarargsNormalized(String className, String alias, String... propertyValues) {
        if (executingInline()) {
            setUpObjects = new SetUpObjectsForConcordion(getScenario().getAliasRegistry(), className, Mode.PERSIST);
            setUpObjects.executeHeader(alias, propertyValues);
            return setUpObjects.executeRow(alias, propertyValues);
        } else {
            if (executingTableHeader()) {
                setUpObjects = new SetUpObjectsForConcordion(getScenario().getAliasRegistry(), className, Mode.PERSIST);
                return setUpObjects.executeHeader(alias, propertyValues);
            } else {
                return setUpObjects.executeRow(alias, propertyValues);
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // using isis viewer
    // ////////////////////////////////////////////////////////////////////////

    private UsingIsisViewerForConcordion usingIsisViewer;

    public String usingIsisViewer(String onObject, String aliasResultAs, String perform, String usingMember) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, null, null, null);
    }

    public String usingIsisViewerThat(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, null, null, null);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0, String arg1) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0, arg1);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0, String arg1, String arg2) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0, arg1, arg2);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0, String arg1, String arg2, String arg3) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0, arg1, arg2,
            arg3);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0, String arg1, String arg2, String arg3, String arg4) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0, arg1, arg2,
            arg3, arg4);
    }

    public String usingIsisViewerArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, null, arg0, arg1, arg2,
            arg3, arg4, arg5);
    }

    public String usingIsisViewerThatArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt, String arg0) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, arg0);
    }

    public String usingIsisViewerThatArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt, String arg0, String arg1) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, arg0, arg1);
    }

    public String usingIsisViewerThatArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt, String arg0, String arg1, String arg2) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, arg0, arg1, arg2);
    }

    public String usingIsisViewerThatArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt, String arg0, String arg1, String arg2, String arg3) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, arg0, arg1, arg2,
            arg3);
    }

    public String usingIsisViewerThatArgs(String onObject, String aliasResultAs, String perform, String usingMember,
        String thatIt, String arg0, String arg1, String arg2, String arg3, String arg4) {
        return usingIsisViewerThatArgsVarargs(onObject, aliasResultAs, perform, usingMember, thatIt, arg0, arg1, arg2,
            arg3, arg4);
    }

    /**
     * With <tt>protected</tt> visibility so that it can be called by custom methods if required.
     */
    protected String usingIsisViewerThatArgsVarargs(String onObject, String aliasResultAs, String perform,
        String usingMember, String thatIt, String arg0, String... remainingArgs) {
        return usingIsisViewerThatArgsVarargsNormalized(StringUtils.normalized(onObject), StringUtils.normalized(aliasResultAs), StringUtils.normalized(perform), StringUtils.normalized(usingMember), StringUtils.normalized(thatIt), StringUtils.normalized(arg0),
            StringUtils.normalized(remainingArgs));
    }

    private String usingIsisViewerThatArgsVarargsNormalized(String onObject, String aliasResultAs, String perform,
        String usingMember, String thatIt, String arg0, String... remainingArgs) {
        if (executingInline()) {
            usingIsisViewer = new UsingIsisViewerForConcordion(getScenario().getAliasRegistry(), getScenario().getDeploymentType(), getScenario().getDateParser(), Perform.Mode.TEST);
            usingIsisViewer.executeHeader(onObject, aliasResultAs, perform, usingMember, thatIt, arg0, remainingArgs);
            return usingIsisViewer.executeRow(onObject, aliasResultAs, perform, usingMember, thatIt, arg0,
                remainingArgs);
        } else {
            if (executingTableHeader()) {
                usingIsisViewer = new UsingIsisViewerForConcordion(getScenario().getAliasRegistry(), getScenario().getDeploymentType(), getScenario().getDateParser(), Perform.Mode.TEST);
                return usingIsisViewer.executeHeader(onObject, aliasResultAs, perform, usingMember, thatIt, arg0,
                    remainingArgs);
            } else {
                return usingIsisViewer.executeRow(onObject, aliasResultAs, perform, usingMember, thatIt, arg0,
                    remainingArgs);
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // check collection
    // ////////////////////////////////////////////////////////////////////////


    public String checkCollectionIsEmpty(String listAlias) {
        return new CheckCollectionContentsForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias)).isEmpty();
    }

    public String checkCollectionIsNotEmpty(String listAlias) {
        return new CheckCollectionContentsForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias)).isNotEmpty();
    }
    
    public String checkCollectionContains(String listAlias, String alias) {
        return new CheckCollectionContentsForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias)).contains(StringUtils.normalized(alias));
    }
    
    public String checkCollectionDoesNotContain(String listAlias, String alias) {
        return new CheckCollectionContentsForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias)).doesNotContain(StringUtils.normalized(alias));
    }
    
    public String checkCollectionSize(String listAlias, int size) {
        return new CheckCollectionContentsForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias)).assertSize(size);
    }
    
    

    // ////////////////////////////////////////////////////////////////////////
    // check list
    // ////////////////////////////////////////////////////////////////////////

    private CheckListForConcordion checkList;
    
    public String checkList(String listAlias, String title) {
        if(executingTable()) {
            if(executingTableHeader()) {
                checkList = new CheckListForConcordion(getScenario().getAliasRegistry(), listAlias);
                return checkList.executeHeader(title);
            } else {
                return checkList.executeRow(title);
            }
        } else {
            checkList = new CheckListForConcordion(getScenario().getAliasRegistry(), listAlias);
            checkList.executeHeader(title);
            return checkList.executeRow(title);
        }
    }


    // ////////////////////////////////////////////////////////////////////////
    // getListContents() (for verifyRow)
    // ////////////////////////////////////////////////////////////////////////
    
    public Iterable<Object> getListContents(String listAlias) {
        ObjectAdapter listAdapter = getScenario().getAliasRegistry().getAliased(StringUtils.normalized(listAlias));
        if(listAdapter == null) {
            return Collections.emptyList();
        }
        CollectionFacet facet = listAdapter.getSpecification().getFacet(CollectionFacet.class);
        if(facet==null){
            return Collections.emptyList();
        }
        Iterable<ObjectAdapter> objectAdapters = facet.iterable(listAdapter);
        return Iterables.transform(objectAdapters, new Function<ObjectAdapter, Object>(){

            @Override
            public Object apply(ObjectAdapter from) {
                return from.getObject();
            }});
    }


    // ////////////////////////////////////////////////////////////////////////
    // alias items in list
    // ////////////////////////////////////////////////////////////////////////

    private AliasItemsInListForConcordion aliasItemsInList;

    public String aliasItemsInList(String listAlias, String title, String aliasAs) {
        return aliasItemsInList(listAlias, title, null, aliasAs);
    }

    public String aliasItemsInList(String listAlias, String title, String type, String aliasAs) {
        aliasItemsInList = new AliasItemsInListForConcordion(getScenario().getAliasRegistry(), StringUtils.normalized(listAlias));
        return aliasItemsInList.execute(StringUtils.normalized(aliasAs), StringUtils.normalized(title), StringUtils.normalized(type));
    }

    private boolean executingTableHeader() {
        return executingTable() && IsisExecuteCommandWithHeader.tableRow.get() == IsisExecuteCommandWithHeader.TableRow.HEADER;
    }

    private boolean executingTable() {
        IsisExecuteCommandWithHeader.Context context = IsisExecuteCommandWithHeader.context.get();
        return context == IsisExecuteCommandWithHeader.Context.TABLE;
    }

    private boolean executingInline() {
        return !executingTable();
    }

    // ////////////////////////////////////////////////////////////////////////
    // run viewer
    // ////////////////////////////////////////////////////////////////////////

    public void runViewer() {
        getScenario().runViewer();
    }

}
