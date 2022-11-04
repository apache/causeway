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
package org.apache.causeway.extensions.secman.applib.permission.dom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 2.0 {@index}
 */
public abstract class ApplicationPermissionRepositoryAbstract<P extends ApplicationPermission>
implements ApplicationPermissionRepository {

    @Inject private RepositoryService repository;
    @Inject private ApplicationFeatureRepository featureRepository;
    @Inject private FactoryService factory;
    @Inject private MessageService messages;

    @Inject private Provider<QueryResultsCache> queryResultsCacheProvider;

    private final Class<P> applicationPermissionClass;

    protected ApplicationPermissionRepositoryAbstract(Class<P> applicationPermissionClass) {
        this.applicationPermissionClass = applicationPermissionClass;
    }

    @Override
    public ApplicationPermission newApplicationPermission() {
        return factory.detachedEntity(applicationPermissionClass);
    }

    // -- findByRole (programmatic)
    public List<ApplicationPermission> findByRoleCached(final @NonNull ApplicationRole role) {
        return queryResultsCacheProvider.get().execute(this::findByRole,
                ApplicationPermissionRepositoryAbstract.class, "findByRoleCached", role);
    }

    public List<ApplicationPermission> findByRole(final @NonNull ApplicationRole role) {
        return _Casts.uncheckedCast(
                repository.allMatches(
                Query.named(this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_ROLE)
                    .withParameter("role", role))
        );
    }


    // -- findByUser (programmatic)
    public List<ApplicationPermission> findByUserCached(final @NonNull ApplicationUser user) {
        return queryResultsCacheProvider.get().execute(this::findByUser,
                ApplicationPermissionRepositoryAbstract.class, "findByUserCached", user);
    }

    public List<ApplicationPermission> findByUser(final @NonNull ApplicationUser user) {
        return findByUser(user.getUsername());
    }

    public List<ApplicationPermission> findByUserMemento(final @NonNull UserMemento userMemento) {
        val roleNames = userMemento.getRoles().stream()
                .map(RoleMemento::getName)
                .collect(Collectors.toList());
        return findByRoleNames(roleNames);
    }

    public List<ApplicationPermission> findByRoleNames(final @NonNull List<String> roleNames) {
        return _Casts.uncheckedCast(
                repository.allMatches(
                        Query.named(this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_ROLE_NAMES)
                                .withParameter("roleNames", roleNames))
        );
    }

    private List<ApplicationPermission> findByUser(final String username) {
        return _Casts.uncheckedCast(
                repository.allMatches(
                Query.named(this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_USER)
                    .withParameter("username", username))
        );
    }


    // -- findByUserAndPermissionValue (programmatic)
    /**
     * Uses the {@link QueryResultsCache} in order to support
     * multiple lookups from <code>org.apache.causeway.extensions.secman.jdo.app.user.UserPermissionViewModel</code>.
     */
    @Override
    public Optional<ApplicationPermission> findByUserAndPermissionValue(final String username, final ApplicationPermissionValue permissionValue) {

        // obtain all permissions for this user, map by its value, and
        // put into query cache (so that this method can be safely called in a tight loop)
        val permissions =
                queryResultsCacheProvider.get().execute(
                        this::permissionsByPermissionValue,
                        ApplicationPermissionRepositoryAbstract.class, "findByUserAndPermissionValue",
                        username);

        // now simply return the permission from the required value (if it exists)
        final List<ApplicationPermission> applicationPermissions = permissions.get(permissionValue);
        return applicationPermissions != null && !applicationPermissions.isEmpty()
                ? Optional.of(applicationPermissions.get(0))
                        : Optional.empty();
    }

    private ListMultimap<ApplicationPermissionValue, ApplicationPermission> permissionsByPermissionValue(
            final String username) {

        // only username (and not permissionValue) is the key
        // (we are obtaining all the perms for this user)

        val permissionsByPermissionValue =
                _Multimaps.<ApplicationPermissionValue, ApplicationPermission>newListMultimap();

        val permissions = findByUser(username);

        _NullSafe.stream(permissions)
        .forEach(permission->{
            val newPermissionValue = ApplicationPermission.Functions.AS_VALUE.apply(permission);
            permissionsByPermissionValue.putElement(newPermissionValue, permission);
        });

        return permissionsByPermissionValue;
    }

    // -- findByRoleAndRuleAndFeatureType (programmatic)
    @Override
    public Collection<ApplicationPermission> findByRoleAndRuleAndFeatureTypeCached(
            org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationFeatureSort type) {
        return queryResultsCacheProvider.get().execute(this::findByRoleAndRuleAndFeatureType,
                ApplicationPermissionRepositoryAbstract.class, "findByRoleAndRuleAndFeatureTypeCached",
                role, rule, type);
    }

    public Collection<ApplicationPermission> findByRoleAndRuleAndFeatureType(
            org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureSort featureSort) {
        return repository.allMatches(Query.named(
                        this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_ROLE_RULE_FEATURE)
                    .withParameter("role", role)
                    .withParameter("rule", rule)
                    .withParameter("featureSort", featureSort))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }


    // -- findByRoleAndRuleAndFeature (programmatic)
    public Optional<ApplicationPermission> findByRoleAndRuleAndFeatureCached(
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureSort featureSort,
            final String featureFqn) {
        return queryResultsCacheProvider.get().execute(
                this::findByRoleAndRuleAndFeature,
                ApplicationPermissionRepositoryAbstract.class, "findByRoleAndRuleAndFeatureCached",
                role, rule, featureSort, featureFqn);
    }

    @Override
    public Optional<ApplicationPermission> findByRoleAndRuleAndFeature(
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureSort featureSort,
            final String featureFqn) {

        return _Casts.uncheckedCast(
                repository
                .uniqueMatch(Query.named(
                                this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_ROLE_RULE_FEATURE_FQN)
                        .withParameter("role", role)
                        .withParameter("rule", rule)
                        .withParameter("featureSort", featureSort)
                        .withParameter("featureFqn", featureFqn ))
        );
    }


    // -- findByFeature (programmatic)

    @Override
    public Collection<ApplicationPermission> findByFeatureCached(final ApplicationFeatureId featureId) {
        return queryResultsCacheProvider.get().execute(
                this::findByFeature, ApplicationPermissionRepositoryAbstract.class, "findByFeatureCached",
                featureId);
    }

    public Collection<ApplicationPermission> findByFeature(final ApplicationFeatureId featureId) {
        return repository.allMatches(
                Query.named(
                        this.applicationPermissionClass, ApplicationPermission.Nq.FIND_BY_FEATURE)
                .withParameter("featureSort", featureId.getSort())
                .withParameter("featureFqn", featureId.getFullyQualifiedName()))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- newPermission (programmatic)

    @Override
    public ApplicationPermission newPermission(
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole genericRole,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureSort featureSort,
            final String featureFqn) {

        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);

        final ApplicationFeatureId featureId = ApplicationFeatureId.newFeature(featureSort, featureFqn);
        final ApplicationFeature feature = featureRepository.findFeature(featureId);
        if(feature == null) {
            messages.warnUser("No such " + featureSort.name().toLowerCase() + ": " + featureFqn);
            return null;
        }
        return newPermissionNoCheck(role, rule, mode, featureSort, featureFqn);
    }

    @Override
    public ApplicationPermission newPermissionNoCheck(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureSort featureSort,
            final String featureFqn) {

        ApplicationPermission permission = findByRoleAndRuleAndFeature(role, rule, featureSort, featureFqn)
                .orElse(null);
        if (permission != null) {
            return permission;
        }
        permission = newApplicationPermission();
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureSort(featureSort);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);
        return permission;
    }

    @Override
    public ApplicationPermission newPermission(
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole genericRole,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String featurePackage,
            final String featureClassName,
            final String featureMemberName) {

        val featureId = ApplicationFeatureId.newFeature(featurePackage, featureClassName, featureMemberName);
        return newPermission(genericRole, rule, mode, featureId);
    }

    @Override
    public ApplicationPermission newPermission(
            final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole genericRole,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureId featureId) {

        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);
        val featureSort = featureId.getSort();
        val featureFqn = featureId.getFullyQualifiedName();

        val feature = featureRepository.findFeature(featureId);
        if(feature == null) {
            messages.warnUser("No such " + featureSort.name().toLowerCase() + ": " + featureFqn);
            return null;
        }

        val permission = factory.detachedEntity(applicationPermissionClass);
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureSort(featureSort);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);

        return permission;
    }


    // -- allPermission (programmatic)
    @Override
    public Collection<ApplicationPermission> allPermissions() {
        return repository.allInstances(this.applicationPermissionClass)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- findOrphaned (programmatic)

    @Override
    public Collection<ApplicationPermission> findOrphaned() {

        val featureNamesKnownToTheMetamodel =
                featureRepository.getFeatureIdentifiersByName().keySet();

        val orphaned = _Lists.<ApplicationPermission>newArrayList();

        for (val permission : allPermissions()) {

            val featId = permission.asFeatureId().orElse(null);
            if(featId==null) {
                orphaned.add(permission);
                continue;
            }

            if(!featureNamesKnownToTheMetamodel.contains(featId.getFullyQualifiedName())) {
                orphaned.add(permission);
            }
        }

        return orphaned;
    }


}
