/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.bdd.common;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderFileSystem;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.AggregatedOid;
import org.apache.isis.core.profilestore.InMemoryUserProfileStoreInstaller;
import org.apache.isis.core.progmodel.facets.value.ValueSemanticsProviderAbstractTemporal;
import org.apache.isis.core.runtime.fixtures.FixturesInstallerNoop;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.installers.InstallerLookupDefault;
import org.apache.isis.core.runtime.runner.IsisInjectModule;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.bdd.common.components.BddAuthenticationManagerInstaller;
import org.apache.isis.viewer.bdd.common.components.BddInMemoryPersistenceMechanismInstaller;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.OpenSession;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.RunViewer;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.SetClock;
import org.apache.isis.viewer.bdd.common.story.bootstrapping.ShutdownIsis;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistryDefault;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistryHolder;

/**
 * Holds the bootstrapped {@link IsisSystem} and provides access to the
 * {@link AliasRegistry aliases}.
 *
 * <p>
 * Typically held in a thread-local by the test framework, acting as a context
 * to the story.
 *
 * <p>
 * Implementation note: this class directly implements {@link AliasRegistrySpi},
 * though delegates to an underlying {@link AliasRegistryDefault}. This is
 * needed because the underlying {@link AliasRegistry} can change on
 * {@link #switchUserWithRoles(String, String)} (see
 * {@link #reAdapt(AliasRegistrySpi)} method).
 */
public class Scenario implements AliasRegistryHolder {

    private AliasRegistry aliasRegistry = new AliasRegistryDefault();

    private DeploymentType deploymentType;
    private String configDirectory;

    private IsisSystem isisSystem;

    private InstallerLookupDefault installerLookup;

    private final DateParser dateParser = new DateParser();

    // /////////////////////////////////////////////////////////////
    // bootstrap / shutdown
    // /////////////////////////////////////////////////////////////

    public String getConfigDirectory() {
        return configDirectory;
    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    public IsisSystem getSystem() {
        return isisSystem;
    }

    public InstallerLookup getInstallerLookup() {
        return installerLookup;
    }

    @SuppressWarnings("unchecked")
    public void bootstrapIsis(final String configDirectory, final DeploymentType deploymentType) {
        this.configDirectory = configDirectory;
        this.deploymentType = ensureThatArg(deploymentType, is(anyOf(equalTo(DeploymentType.EXPLORATION), equalTo(DeploymentType.PROTOTYPE))));

        final IsisConfigurationBuilderFileSystem configurationBuilder = new IsisConfigurationBuilderFileSystem(getConfigDirectory());

        configurationBuilder.add(SystemConstants.DEPLOYMENT_TYPE_KEY, deploymentType.name());
        defaultStoryComponentsInto(configurationBuilder);

        getAliasRegistry().clear();

        try {
            // create system...
            isisSystem = createSystem(deploymentType, configurationBuilder);

            // ... and provide a session in order to install fixtures
            IsisContext.openSession(new InitialisationSession());

        } catch (final RuntimeException e) {
            if (isisSystem != null) {
                isisSystem.shutdown();
            }
            throw e;
        }
    }

    private IsisSystem createSystem(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder) {
        this.installerLookup = new InstallerLookupDefault();
        isisConfigurationBuilder.injectInto(installerLookup);

        final Injector injector = createGuiceInjector(deploymentType, isisConfigurationBuilder, installerLookup);
        final IsisSystem system = injector.getInstance(IsisSystem.class);
        return system;
    }

    private void defaultStoryComponentsInto(final IsisConfigurationBuilder isisConfigurationBuilder) {
        isisConfigurationBuilder.addDefaultConfigurationResources();
        isisConfigurationBuilder.add(SystemConstants.AUTHENTICATION_INSTALLER_KEY, BddAuthenticationManagerInstaller.class.getName());
        isisConfigurationBuilder.add(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY, BddInMemoryPersistenceMechanismInstaller.class.getName());
        isisConfigurationBuilder.add(SystemConstants.PROFILE_PERSISTOR_INSTALLER_KEY, InMemoryUserProfileStoreInstaller.class.getName());
        isisConfigurationBuilder.add(SystemConstants.FIXTURES_INSTALLER_KEY, FixturesInstallerNoop.class.getName());
        isisConfigurationBuilder.add(SystemConstants.NOSPLASH_KEY, "" + true);
    }

    private Injector createGuiceInjector(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder, final InstallerLookup installerLookup) {
        final IsisInjectModule isisModule = new IsisInjectModule(deploymentType, isisConfigurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }

    public void shutdownIsis() {
        new ShutdownIsis(this).shutdown();
    }

    // /////////////////////////////////////////////////////////////
    // date+time / logon+switch user
    // /////////////////////////////////////////////////////////////

    public boolean dateAndTimeIs(final String dateAndTimeStr) {
        final Date date = dateParser.parse(dateAndTimeStr);
        if (date != null) {
            new SetClock(this).setClock(date);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Logon, specifying no roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular fixtures, the logonAs is not
     * automatically remembered until the end of the setup. It should therefore
     * be invoked at the end of setup explicitly.
     */
    public void logonAsOrSwitchUserTo(final String userName) {
        final List<String> noRoles = Collections.emptyList();
        logonAsOrSwitchUserTo(userName, noRoles);
    }

    /**
     * Logon, specifying roles.
     * <p>
     * Unlike the {@link LogonFixture} on regular Isis fixtures, the logonAs is
     * not automatically remembered until the end of the setup. It should
     * therefore be invoked at the end of setup explicitly.
     */
    public void logonAsOrSwitchUserTo(final String userName, final List<String> roleList) {
        new OpenSession(this).openSession(userName, roleList);
        aliasRegistry = reAdapt(aliasRegistry);
    }

    /**
     * Need to recreate aliases whenever logout/login.
     */
    private AliasRegistry reAdapt(final AliasRegistry aliasesRegistry) {
        final AliasRegistry newAliasesRegistry = new AliasRegistryDefault();

        // first pass: root adapters
        for (final Map.Entry<String, ObjectAdapter> aliasAdapter : aliasesRegistry) {
            final String alias = aliasAdapter.getKey();
            final ObjectAdapter oldAdapter = aliasAdapter.getValue();

            if (oldAdapter.getOid() instanceof AggregatedOid) {
                continue;
            }
            newAliasesRegistry.aliasAs(alias, getAdapterManager().adapterFor(oldAdapter.getObject()));
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

    // /////////////////////////////////////////////////////////
    // date parser
    // /////////////////////////////////////////////////////////

    public void usingDateFormat(final String dateFormatStr) {
        dateParser.setDateFormat(dateFormatStr);
        setTemporalFormat("date", dateFormatStr);
        setTemporalFormat("datetime", dateParser.getCombinedMask());
        setTemporalFormat("timestamp", dateParser.getCombinedMask());
    }

    public void usingTimeFormat(final String timeFormatStr) {
        dateParser.setTimeFormat(timeFormatStr);
        setTemporalFormat("time", timeFormatStr);
        setTemporalFormat("datetime", dateParser.getCombinedMask());
        setTemporalFormat("timestamp", dateParser.getCombinedMask());
    }

    private void setTemporalFormat(final String propertyType, final String formatStr) {
        ValueSemanticsProviderAbstractTemporal.setFormat(propertyType, formatStr);
    }

    public DateParser getDateParser() {
        return dateParser;
    }

    // /////////////////////////////////////////////////////////
    // run viewer
    // /////////////////////////////////////////////////////////

    public void runViewer() {
        new RunViewer(this).run();
    }

    // //////////////////////////////////////////////////////////////////
    // AliasRegistry impl
    // //////////////////////////////////////////////////////////////////

    @Override
    public AliasRegistry getAliasRegistry() {
        return aliasRegistry;
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

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

}
