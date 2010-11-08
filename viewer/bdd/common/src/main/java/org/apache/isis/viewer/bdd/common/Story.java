package org.apache.isis.viewer.bdd.common;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.IsisSystem;
import org.apache.isis.runtime.transaction.IsisTransactionManager;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.IsisInitializer;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.OpenSession;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.SetClock;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.ShutdownNakedObjects;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.StartClient;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistryDefault;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistrySpi;
import org.apache.isis.viewer.bdd.common.util.Strings;

/**
 * Holds the bootstrapped {@link NakedObjectsSystem} and provides access to
 * the {@link AliasRegistry aliases}.
 * 
 * <p>
 * Typically held in a thread-local by the test framework, acting as a context to the story.
 * 
 * <p>
 * Implementation note: this class directly implements {@link AliasRegistrySpi}, though delegates to an
 * underlying {@link AliasRegistryDefault}.  This is needed because the underlying {@link AliasRegistry}
 * can change on {@link #switchUserWithRoles(String, String)} (see {@link #reAdapt(AliasRegistrySpi)} method).
 */
public class Story implements StoryBootstrapper, AliasRegistrySpi {

	private AliasRegistrySpi aliasRegistry = new AliasRegistryDefault();

	private String configDirectory;
    private boolean exploration;
    
	private InstallerLookup installerLookup;
	private IsisSystem nakedObjectsSystem;


    public String getConfigDirectory() {
        return configDirectory;
    }
    public void setConfigDirectory(final String configDirectory) {
    	this.configDirectory = configDirectory;
    }

    public void enableExploration() {
    	this.exploration = true;
    }
    
    public DeploymentType getDeploymentType() {
        return exploration ? DeploymentType.EXPLORATION
                : DeploymentType.PROTOTYPE;
    }

    public InstallerLookup getInstallerLookup() {
        return installerLookup;
    }
    public void setInstallerLookup(final InstallerLookup installerLookup) {
        this.installerLookup = installerLookup;
    }

    public IsisSystem getSystem() {
        return nakedObjectsSystem;
    }

    /**
     * Called by {@link IsisInitializer}.
     */
    public void setIsisSystem(
            final IsisSystem nakedObjectsSystem) {
        this.nakedObjectsSystem = nakedObjectsSystem;
    }

    
    /**
     * Logon, specifying no roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular Naked Objects fixtures, the
     * logonAs is not automatically remembered until the end of the setup. It
     * should therefore be invoked at the end of setup explicitly.
     */
    public void logonAs(final String userName) {
        switchUser(userName);
    }

    /**
     * Logon, specifying roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular Naked Objects fixtures, the
     * logonAs is not automatically remembered until the end of the setup. It
     * should therefore be invoked at the end of setup explicitly.
     */
    public void logonAsWithRoles(final String userName, final String roleList) {
        switchUserWithRoles(userName, roleList);
    }

    /**
     * Switch user, specifying no roles.
     */
    public void switchUser(final String userName) {
        switchUserWithRoles(userName, null);
    }

    /**
     * Switch user, specifying roles.
     */
    public void switchUserWithRoles(final String userName, final String roleList) {
        new OpenSession(this).openSession(userName, Strings
                .splitOnCommas(roleList));
        aliasRegistry = reAdapt(aliasRegistry);
    }

    /**
     * Need to recreate aliases whenever logout/login.
     */
    private AliasRegistrySpi reAdapt(final AliasRegistrySpi aliasesRegistrySpi) {
        final AliasRegistrySpi newAliasesRegistry = new AliasRegistryDefault();

        // first pass: root adapters
        for (final Map.Entry<String, ObjectAdapter> aliasAdapter : aliasesRegistrySpi) {
            final String alias = aliasAdapter.getKey();
            final ObjectAdapter oldAdapter = aliasAdapter.getValue();

            if (oldAdapter.getOid() instanceof AggregatedOid) {
                continue;
            }
            newAliasesRegistry.alias(alias, getAdapterManager().adapterFor(
                    oldAdapter.getObject()));
        }

        // for now, not supporting aggregated adapters (difficulty in looking up
        // the parent adapter because the Oid changes)

        // // second pass: aggregated adapters
        // for (Map.Entry<String,NakedObject> aliasAdapter : oldAliasesRegistry)
        // {
        // final String alias = aliasAdapter.getKey();
        // final ObjectAdapter oldAdapter = aliasAdapter.getValue();
        //			
        // if(!(oldAdapter.getOid() instanceof AggregatedOid)) {
        // continue;
        // }
        // AggregatedOid aggregatedOid = (AggregatedOid) oldAdapter.getOid();
        // final Object parentOid = aggregatedOid.getParentOid();
        // final ObjectAdapter parentAdapter =
        // getAdapterManager().getAdapterFor(parentOid);
        // final String fieldName = aggregatedOid.getFieldName();
        // final NakedObjectAssociation association =
        // parentAdapter.getSpecification().getAssociation(fieldName);
        // final ObjectAdapter newAdapter =
        // getAdapterManager().adapterFor(oldAdapter.getObject(), parentAdapter,
        // association);
        //			
        // newAliasesRegistry.put(alias, newAdapter);
        // }
        return newAliasesRegistry;
    }

    public void registerService(final String aliasAs, final String serviceClassName) throws StoryValueException {
        aliasRegistry.aliasService(aliasAs, serviceClassName);
    }


    /**
     * Holds a new {@link NakedObject adapter}, automatically assigning it a new
     * heldAs alias.
     */
    public String aliasPrefixedAs(final String prefix, final ObjectAdapter adapter) {
        return aliasRegistry.aliasPrefixedAs(prefix, adapter);
    }

    /**
     * Holds a new {@link ObjectAdapter}.
     */
    public void aliasAs(final String alias, final ObjectAdapter adapter) {
        aliasRegistry.aliasAs(alias, adapter);
    }

    public void dateIsNow(final Date dateAndTime) {
        dateIs(dateAndTime);
    }

    public void dateIs(final Date dateAndTime) {
        timeIs(dateAndTime);
    }

    public void timeIsNow(final Date dateAndTime) {
        timeIs(dateAndTime);
    }

    public void timeIs(final Date dateAndTime) {
        new SetClock(this).setClock(dateAndTime);
    }

    public void shutdownNakedObjects() {
        new ShutdownNakedObjects(this).shutdown();
    }

    public void runViewer() {
        new StartClient(this).run();
    }

    
    // /////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    public IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }
    
    
    ////////////////////////////////////////////////////////////////////
    // AliasRegistry impl
    ////////////////////////////////////////////////////////////////////
    
	public String getAlias(ObjectAdapter adapter) {
		return aliasRegistry.getAlias(adapter);
	}
	public ObjectAdapter getAliased(String alias) {
		return aliasRegistry.getAliased(alias);
	}
	public void alias(String alias, ObjectAdapter adapter) {
		aliasRegistry.alias(alias, adapter);
	}
	public void aliasService(String aliasAs, String className)
			throws StoryValueException {
		aliasRegistry.aliasService(aliasAs, className);
	}
	public Iterator<Entry<String, ObjectAdapter>> iterator() {
		return aliasRegistry.iterator();
	}
    

}
