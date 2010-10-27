package org.apache.isis.viewer.bdd.concordion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.isis.commons.io.IoUtils;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.metamodel.config.ConfigurationBuilderFileSystem;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.Story;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer.Mode;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.InitNakedObjects;
import org.apache.isis.viewer.bdd.concordion.internal.concordion.ExecuteCommandWithHeader;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.AliasItemsInListForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.AliasServicesForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.SetUpObjectsForConcordion;
import org.apache.isis.viewer.bdd.concordion.internal.fixtures.UsingNakedObjectsViewerForConcordion;
import org.concordion.Concordion;
import org.concordion.api.ResultSummary;
import org.concordion.internal.ConcordionBuilder;
import org.concordion.internal.FileTarget;
import org.junit.Test;

public class AbstractNakedObjectsConcordionTest {

	public static final String DEFAULT_CONCORDION_CSS = "concordion.css";
	public static final String DEFAULT_TARGET_DIR = "/tmp/concordion";
	
	private final static String NS_URI = "http://testedobjects.sourceforge.net/2010/concordion";
	private static final String CMD_EXECUTE = "execute";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd MMM yyyy hh:mm");

	private static ThreadLocal<Story> storyThreadLocal = new ThreadLocal<Story>() {
		@Override
		protected Story initialValue() {
			return new Story();
		}
	};

	protected static Story getStory() {
		return storyThreadLocal.get();
	}

	@Test
	public void runStory() throws Throwable {
		Concordion concordion = createConcordion();
		ResultSummary resultSummary = concordion.process(this);
		resultSummary.print(System.out, this);
		resultSummary.assertIsSatisfied(this);
		copyCustomCssIfDefined();
	}

	private void copyCustomCssIfDefined()  {
		Class<?> cssClass = customCssPackage();
		String customCss = customCss();
		if (cssClass == null || customCss == null) {
			return;
		}
		InputStream cssInputFile = cssClass.getResourceAsStream(customCss);
		String cssPackageName = cssClass.getPackage().getName();
		String cssPackagePath = asPath(cssPackageName);
		String cssOutputFileName = combine(targetDir(), cssPackagePath, customCss);
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

	/**
	 * Optional hook method to specify the directory to which the processed HTML should be
	 * copied.
	 * 
	 * <p>
	 * Defaults to {@value #DEFAULT_TARGET_DIR}.
	 */
	protected String targetDir() {
		return DEFAULT_TARGET_DIR;
	}

	/**
	 * Optional hook method to specify the class (any class) that resides in the same package as the {@link #customCss()}.
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
	 * Defaults to {@value #DEFAULT_CONCORDION_CSS} so you do not need to override if
	 * your custom CSS file has this name.  However, it is necessary to override
	 * {@link #customCssPackage()} to indicate the package that the CSS resides in.
	 * 
	 * @see #customCssPackage()
	 */
	protected String customCss() {
		return DEFAULT_CONCORDION_CSS;
	}

	private String combine(String tmpDir, String... paths) {
		StringBuilder buf = new StringBuilder(tmpDir);
		for(String path: paths) {
			if (buf.charAt(buf.length()-1) != File.separatorChar) {
				buf.append(File.separatorChar);	
			}
			buf.append(path);
		}
		return buf.toString();
	}

	private Concordion createConcordion() {
		if (targetDir() == null) {
			throw new IllegalStateException("targetDir() cannot be null");
		}
		ConcordionBuilder builder = new ConcordionBuilder()
			.withTarget(new FileTarget(new File(targetDir())))
			.withCommand(NS_URI, CMD_EXECUTE, new ExecuteCommandWithHeader());
		return builder.build();
	}

	@SuppressWarnings("unused")
	private ConfigurationBuilder configurationBuilder;

	public String getConfigDirectory() {
		return getStory().getConfigDirectory();
	}

	public DeploymentType getDeploymentType() {
		return getStory().getDeploymentType();
	}

	public boolean initNakedObjects(String configDirectory,
			String deploymentType) {
		getStory().setConfigDirectory(configDirectory);
		if (DeploymentType.EXPLORATION.toString().equalsIgnoreCase(
				deploymentType)) {
			getStory().enableExploration();
		}
		ConfigurationBuilderFileSystem configurationBuilder = new ConfigurationBuilderFileSystem(
				getConfigDirectory());
		InitNakedObjects initializer = new InitNakedObjects(
				getStory(), configurationBuilder, getDeploymentType());
		initializer.initialize();
		return true; // any runtime exception will propogate
	}

	public void aliasAs(String alias, ObjectAdapter adapter) {
		getStory().aliasAs(alias, adapter);
	}

	public String aliasPrefixedAs(String prefix, ObjectAdapter adapter) {
		return getStory().aliasPrefixedAs(prefix, adapter);
	}

	public void aliasServiceAs(String alias, String serviceClassName)
			throws StoryValueException {
		getStory().registerService(alias, serviceClassName);
	}

	public String getAlias(ObjectAdapter adapter) {
		return getStory().getAlias(adapter);
	}

	public ObjectAdapter getAliased(String alias) {
		return getStory().getAliased(alias);
	}

	public boolean logonAs(String userName) {
		getStory().logonAs(userName);
		return true;
	}

	public boolean logonAsWithRoles(String userName, String roleList) {
		getStory().logonAsWithRoles(userName, roleList);
		return true;
	}

	public void switchUser(String userName) {
		getStory().switchUser(userName);
	}

	public void switchUserWithRoles(String userName, String roleList) {
		getStory().switchUserWithRoles(userName, roleList);
	}

	public boolean dateIs(String dateAndTimeStr) throws StoryValueException {
		getStory().dateIs(asDateAndTime(dateAndTimeStr));
		return true;
	}

	public boolean dateIsNow(String dateAndTimeStr) throws StoryValueException {
		getStory().dateIsNow(asDateAndTime(dateAndTimeStr));
		return true;
	}

	public boolean timeIs(String dateAndTimeStr) throws StoryValueException {
		getStory().timeIs(asDateAndTime(dateAndTimeStr));
		return true;
	}

	public boolean timeIsNow(String dateAndTimeStr) throws StoryValueException {
		getStory().timeIsNow(asDateAndTime(dateAndTimeStr));
		return true;
	}

	private Date asDateAndTime(String dateAndTimeStr)
			throws StoryValueException {
		try {
			Date dateAndTime = DATE_FORMAT.parse(dateAndTimeStr);
			return dateAndTime;
		} catch (ParseException e) {
			throw new StoryValueException(e);
		}
	}

	private AliasServicesForConcordion aliasServices;

	public boolean aliasService(String className, String alias) {
		return aliasServices(className, alias);
	}

	public boolean aliasServices(String className, String alias) {
		if (aliasServices == null) {
			aliasServices = new AliasServicesForConcordion(getStory());
		}
		aliasServices.execute(alias, className);
		return true;
	}

	public void shutdownNakedObjects() {
		getStory().shutdownNakedObjects();
	}

	public String setUpObject(String className, String alias, String arg0) {
		return setUpObjectsVarargs(className, alias, arg0);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1) {
		return setUpObjectsVarargs(className, alias, arg0, arg1);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4, String arg5) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4, arg5);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4, String arg5,
			String arg6) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4, arg5, arg6);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4, String arg5,
			String arg6, String arg7) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4, arg5, arg6, arg7);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4, String arg5,
			String arg6, String arg7, String arg8) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4, arg5, arg6, arg7, arg8);
	}

	public String setUpObject(String className, String alias, String arg0,
			String arg1, String arg2, String arg3, String arg4, String arg5,
			String arg6, String arg7, String arg8, String arg9) {
		return setUpObjectsVarargs(className, alias, arg0, arg1, arg2, arg3,
				arg4, arg5, arg6, arg7, arg8, arg9);
	}

	private SetUpObjectsForConcordion setUpObjects;

	/**
	 * Workaround for OGNL defect.
	 */
	private String setUpObjectsVarargs(String className, String alias,
			String... propertyValues) {
		if (executingInline()) {
			setUpObjects = new SetUpObjectsForConcordion(getStory(), className,
					Mode.PERSIST);
			setUpObjects.executeHeader(alias, propertyValues);
			return setUpObjects.executeRow(alias, propertyValues);
		} else {
			if (executingTableHeader()) {
				setUpObjects = new SetUpObjectsForConcordion(getStory(), className,
						Mode.PERSIST);
				return setUpObjects.executeHeader(alias, propertyValues);
			} else {
				return setUpObjects.executeRow(alias, propertyValues);
			}
		}
	}

	private UsingNakedObjectsViewerForConcordion usingNakedObjectsViewer;

	public String usingNakedObjectsViewer(String onObject,
			String aliasResultAs, String perform, String usingMember) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, null, null, null);
	}

	public String usingNakedObjectsViewerThat(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, null, null, null);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0, String arg1) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0, arg1);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0, String arg1, String arg2) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0, arg1, arg2);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0, String arg1, String arg2, String arg3) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0, arg1, arg2, arg3);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0, String arg1, String arg2, String arg3, String arg4) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0, arg1, arg2, arg3, arg4);
	}

	public String usingNakedObjectsViewerArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String arg0, String arg1, String arg2, String arg3, String arg4,
			String arg5) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, null, arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public String usingNakedObjectsViewerThatArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, arg0);
	}

	public String usingNakedObjectsViewerThatArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0, String arg1) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, arg0, arg1);
	}

	public String usingNakedObjectsViewerThatArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0, String arg1, String arg2) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, arg0, arg1, arg2);
	}

	public String usingNakedObjectsViewerThatArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0, String arg1, String arg2, String arg3) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, arg0, arg1, arg2, arg3);
	}

	public String usingNakedObjectsViewerThatArgs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0, String arg1, String arg2, String arg3,
			String arg4) {
		return usingNakedObjectsViewerThatArgsVarargs(onObject, aliasResultAs,
				perform, usingMember, thatIt, arg0, arg1, arg2, arg3, arg4);
	}

	private String usingNakedObjectsViewerThatArgsVarargs(String onObject,
			String aliasResultAs, String perform, String usingMember,
			String thatIt, String arg0, String... remainingArgs) {
		if (executingInline()) {
			usingNakedObjectsViewer = new UsingNakedObjectsViewerForConcordion(
					getStory(), Perform.Mode.TEST);
			usingNakedObjectsViewer.executeHeader(onObject, aliasResultAs,
					perform, usingMember, thatIt, arg0, remainingArgs);
			return usingNakedObjectsViewer.executeRow(onObject, aliasResultAs,
					perform, usingMember, thatIt, arg0, remainingArgs);
		} else {
			if (executingTableHeader()) {
				usingNakedObjectsViewer = new UsingNakedObjectsViewerForConcordion(
						getStory(), Perform.Mode.TEST);
				return usingNakedObjectsViewer.executeHeader(onObject, aliasResultAs,
						perform, usingMember, thatIt, arg0, remainingArgs);
			} else {
				return usingNakedObjectsViewer.executeRow(onObject, aliasResultAs,
						perform, usingMember, thatIt, arg0, remainingArgs);
			}
		}
	}

	private AliasItemsInListForConcordion aliasItemsInList;

	public String aliasItemsInList(String listAlias, String title,
			String aliasAs) {
		return aliasItemsInList(listAlias, title, null, aliasAs);
	}

	public String aliasItemsInList(String listAlias, String title, String type,
			String aliasAs) {
		aliasItemsInList = new AliasItemsInListForConcordion(getStory(),
				listAlias);
		return aliasItemsInList.execute(aliasAs, title, type);
	}

	private boolean executingTableHeader() {
		return executingTable() && 
			   ExecuteCommandWithHeader.tableRow
			.get() == ExecuteCommandWithHeader.TableRow.HEADER;
	}

	private boolean executingTable() {
		ExecuteCommandWithHeader.Context context = ExecuteCommandWithHeader.context
		.get();
		return context == ExecuteCommandWithHeader.Context.TABLE;
	}

	private boolean executingInline() {
		return !executingTable();
	}

	public void runViewer() {
		getStory().runViewer();
	}

}
