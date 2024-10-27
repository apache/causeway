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
package org.apache.causeway.extensions.secman.applib.seed.scripts;

import java.io.File;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtLayoutLoadersRoleAndPermissions;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.extensions.secman.applib.role.fixtures.AbstractRoleAndPermissionsFixtureScript;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayAppFeatureRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayConfigurationRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtAuditTrailRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtCommandLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtDocgenRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtExecutionLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtExecutionOutboxRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtH2ConsoleRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSecmanAdminRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSecmanRegularUserRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayExtSessionLogRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayPersistenceJdoMetaModelRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewaySudoImpersonateRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.role.seed.CausewayViewerRestfulObjectsSwaggerRoleAndPermissions;
import org.apache.causeway.extensions.secman.applib.seed.SeedSecurityModuleService;
import org.apache.causeway.extensions.secman.applib.tenancy.fixtures.AbstractTenancyFixtureScript;
import org.apache.causeway.extensions.secman.applib.tenancy.seed.GlobalTenancy;
import org.apache.causeway.extensions.secman.applib.user.fixtures.AbstractUserAndRolesFixtureScript;
import org.apache.causeway.extensions.secman.applib.user.seed.CausewayExtSecmanAdminUser;
import org.apache.causeway.extensions.secman.applib.util.ApplicationSecurityDto;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.extern.log4j.Log4j2;

/**
 * Sets up roles and permissions for both Secman itself and also for all other modules that expose UI features
 * for use by end-users.
 *
 * <p>
 * This fixture script is run automatically on start-up by the {@link SeedSecurityModuleService}.
 * </p>
 *
 * @see SeedSecurityModuleService
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class SeedUsersAndRolesFixtureScript extends FixtureScript {

    @Inject private CausewayConfiguration config;
    @Inject private CausewayBeanTypeRegistry causewayBeanTypeRegistry;
    @Inject private ValueSemanticsProvider<java.util.Locale> localeSemantics;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        var secmanConfig = config.getExtensions().getSecman();
        var persistenceStack = causewayBeanTypeRegistry.determineCurrentPersistenceStack();

        // used as log message provider below - assuming file was found and is readable
        final Supplier<String> yamlFilePath = ()->new File(secmanConfig.getSeed().getYamlFile()).getAbsolutePath();

        // if a config option ..secman.seed.yamlFile is present,
        // try to use it as alternative seeding strategy,
        // then exit
        final ApplicationSecurityDto dto = _Strings.nonEmpty(secmanConfig.getSeed().getYamlFile())
                .map(File::new)
                .filter(File::exists)
                .filter(File::canRead)
                .map(DataSource::ofFile)
                .map(ApplicationSecurityDto::tryRead)
                .map(_try->_try.ifFailure(ex->log.error(String.format("failed seeding from YAML %s", yamlFilePath.get()), ex)))
                .orElseGet(()->Try.success(null))
                .getValue()
                .orElse(null);
        if(dto!=null) {
            log.info("seeding from YAML file {}", yamlFilePath.get());
            seedFromDto(executionContext, dto);
            return; // exit, don't process further
        }

        // global tenancy
        executionContext.executeChild(this, new GlobalTenancy());

        // modules
        executionContext.executeChildren(this,
                new CausewayAppFeatureRoleAndPermissions(),
                persistenceStack == PersistenceStack.JDO
                    ? new CausewayPersistenceJdoMetaModelRoleAndPermissions()
                    : null, // skip if non-JDO deployment
                new CausewayExtAuditTrailRoleAndPermissions(),
                new CausewayExtCommandLogRoleAndPermissions(),
                new CausewayExtDocgenRoleAndPermissions(),
                new CausewayExtExecutionLogRoleAndPermissions(),
                new CausewayExtExecutionOutboxRoleAndPermissions(),
                new CausewayExtLayoutLoadersRoleAndPermissions(),
                new CausewayExtSessionLogRoleAndPermissions(),
                new CausewayExtH2ConsoleRoleAndPermissions(),
                new CausewayViewerRestfulObjectsSwaggerRoleAndPermissions(),
                new CausewaySudoImpersonateRoleAndPermissions(),
                new CausewayConfigurationRoleAndPermissions()
                );

        // secman module (admin and regular users role, and secman-admin superuser)
        executionContext.executeChildren(this,
                new CausewayExtSecmanAdminRoleAndPermissions(secmanConfig),
                new CausewayExtSecmanRegularUserRoleAndPermissions(secmanConfig),
                new CausewayExtSecmanAdminUser(secmanConfig,
                        CausewayAppFeatureRoleAndPermissions.ROLE_NAME,
                        persistenceStack == PersistenceStack.JDO
                            ? CausewayPersistenceJdoMetaModelRoleAndPermissions.ROLE_NAME
                            : null, // skip if non-JDO deployment
                        CausewayExtAuditTrailRoleAndPermissions.ROLE_NAME,
                        CausewayExtCommandLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtDocgenRoleAndPermissions.ROLE_NAME,
                        CausewayExtExecutionLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtExecutionOutboxRoleAndPermissions.ROLE_NAME,
                        CausewayExtSessionLogRoleAndPermissions.ROLE_NAME,
                        CausewayExtH2ConsoleRoleAndPermissions.ROLE_NAME,
                        CausewayViewerRestfulObjectsSwaggerRoleAndPermissions.ROLE_NAME,
                        CausewaySudoImpersonateRoleAndPermissions.ROLE_NAME,
                        CausewayConfigurationRoleAndPermissions.ROLE_NAME)
                );

    }

    // -- HELPER

    private void seedFromDto(final ExecutionContext executionContext, final ApplicationSecurityDto dto) {

        // TENANCIES

        _NullSafe.stream(dto.getTenancies())
        .sorted((a, b)->{
            // sort, such that dependencies come before dependents
            final int lenA = _NullSafe.size(a.getParentPath());
            final int lenB = _NullSafe.size(b.getParentPath());
            return Integer.compare(lenA, lenB);
        })
        .forEach(tenancyDto->{
            executionContext.executeChild(this, new AbstractTenancyFixtureScript() {
                @Override
                public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
                    return FixtureScripts.MultipleExecutionStrategy.EXECUTE;
                }
                @Override
                protected void execute(ExecutionContext executionContext) {
                    create(tenancyDto.get__name(), tenancyDto.getPath(),
                            tenancyDto.getParentPath(), executionContext);
                }
            });
        });

        // ROLES

        _NullSafe.stream(dto.getRoles())
        .forEach(roleDto->{
            executionContext.executeChildren(this,
                    new AbstractRoleAndPermissionsFixtureScript(
                            roleDto.get__name(), roleDto.getDescription()) {
                @Override
                public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
                    return FixtureScripts.MultipleExecutionStrategy.EXECUTE;
                }
                @Override
                protected void execute(ExecutionContext executionContext) {

                    // PERMISSIONS

                    _NullSafe.stream(roleDto.getPermissions())
                    .forEach(permissionDto->{
                        newPermissions(
                                permissionDto.getRule(),
                                permissionDto.getMode(),
                                Can.of(
                                        ApplicationFeatureId.newFeature(
                                                permissionDto.getFeatureSort(),
                                                permissionDto.getFeatureFqn())
                                        )
                        );
                    });
                }
            });
        });

        // USERS

        _NullSafe.stream(dto.getUsers())
        .forEach(userDto->{
            executionContext.executeChildren(this,
                    new AbstractUserAndRolesFixtureScript(
                            userDto.get__username(),
                            "pass", // to be overwritten below
                            userDto.getAccountType(),
                            Can.ofCollection(userDto.getRoleNames())) {
                @Override
                public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
                    return FixtureScripts.MultipleExecutionStrategy.EXECUTE;
                }
                @Override
                protected void execute(final ExecutionContext executionContext) {
                    super.execute(executionContext);
                    getApplicationUser().setEncryptedPassword(userDto.getEncryptedPassword());
                    getApplicationUser().setAtPath(userDto.getAtPath());
                    getApplicationUser().setFamilyName(userDto.getFamilyName());
                    getApplicationUser().setGivenName(userDto.getGivenName());
                    getApplicationUser().setKnownAs(userDto.getKnownAs());
                    getApplicationUser().setEmailAddress(userDto.getEmailAddress());
                    getApplicationUser().setPhoneNumber(userDto.getPhoneNumber());
                    getApplicationUser().setFaxNumber(userDto.getFaxNumber());
                    getApplicationUser().setLanguage(parseLocale(userDto.getLanguage()));
                    getApplicationUser().setNumberFormat(parseLocale(userDto.getNumberFormat()));
                    getApplicationUser().setTimeFormat(parseLocale(userDto.getTimeFormat()));
                    getApplicationUser().setStatus(userDto.getStatus());
                }
            });
        });

    }

    // -- HELPER

    @Nullable
    private java.util.Locale parseLocale(final @Nullable String input) {
        return localeSemantics.getParser().parseTextRepresentation(null, input);
    }

}
