package org.apache.isis.viewer.bdd.common;

import java.util.Date;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;


public interface StoryBootstrapper  {
	
	void setConfigDirectory(final String configDirectory);
	String getConfigDirectory();

	void enableExploration();
	DeploymentType getDeploymentType();
	
    InstallerLookup getInstallerLookup();
	void setInstallerLookup(InstallerLookup installerLookup);

	IsisSystem getSystem();
	void setNakedObjectsSystem(IsisSystem system);


    /**
     * Logon, specifying no roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular Naked Objects fixtures, the
     * logonAs is not automatically remembered until the end of the setup. It
     * should therefore be invoked at the end of setup explicitly.
     */
    void logonAs(final String userName);

    /**
     * Logon, specifying roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular Naked Objects fixtures, the
     * logonAs is not automatically remembered until the end of the setup. It
     * should therefore be invoked at the end of setup explicitly.
     */
    void logonAsWithRoles(final String userName, final String roleList);

    /**
     * Switch user, specifying no roles.
     */
    void switchUser(final String userName);

    /**
     * Switch user, specifying roles.
     */
    void switchUserWithRoles(final String userName, final String roleList);
    

    public void dateIsNow(final Date dateAndTime);
    public void dateIs(final Date dateAndTime);

    public void timeIsNow(final Date dateAndTime);
    public void timeIs(final Date dateAndTime);


    public void runViewer();

    public void shutdownNakedObjects();
}