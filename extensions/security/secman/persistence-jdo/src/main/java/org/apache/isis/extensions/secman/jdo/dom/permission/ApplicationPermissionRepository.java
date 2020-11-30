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
package org.apache.isis.extensions.secman.jdo.dom.permission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;

import lombok.NonNull;
import lombok.val;

@Repository
@Named("isisExtSecman.applicationPermissionRepository")
public class ApplicationPermissionRepository
implements org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository<ApplicationPermission> {

    @Inject private RepositoryService repository;
    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;
    @Inject private FactoryService factory;
    @Inject private MessageService messages;
    
    @Inject private javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    @Override
    public ApplicationPermission newApplicationPermission() {
        return factory.detachedEntity(new ApplicationPermission());
    }

    // -- findByRole (programmatic)
    public List<ApplicationPermission> findByRoleCached(@NonNull final ApplicationRole role) {
        return queryResultsCacheProvider.get().execute(this::findByRole,
                ApplicationPermissionRepository.class, "findByRoleCached", role);
    }

    public List<ApplicationPermission> findByRole(@NonNull final ApplicationRole role) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByRole",
                        "role", role));
    }


    // -- findByUser (programmatic)
    public List<ApplicationPermission> findByUserCached(@NonNull final ApplicationUser user) {
        return queryResultsCacheProvider.get().execute(this::findByUser, 
                ApplicationPermissionRepository.class, "findByUserCached", user);
    }

    public List<ApplicationPermission> findByUser(@NonNull final ApplicationUser user) {
        return findByUser(user.getUsername());
    }

    private List<ApplicationPermission> findByUser(final String username) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByUser",
                        "username", username));
    }


    // -- findByUserAndPermissionValue (programmatic)
    /**
     * Uses the {@link QueryResultsCache} in order to support
     * multiple lookups from <code>org.apache.isis.extensions.secman.jdo.app.user.UserPermissionViewModel</code>.
     */
    @Override
    public Optional<ApplicationPermission> findByUserAndPermissionValue(final String username, final ApplicationPermissionValue permissionValue) {

        // obtain all permissions for this user, map by its value, and
        // put into query cache (so that this method can be safely called in a tight loop)
        val permissions =
                queryResultsCacheProvider.get().execute(
                        this::permissionsByPermissionValue, 
                        ApplicationPermissionRepository.class, "findByUserAndPermissionValue", 
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
            org.apache.isis.extensions.secman.api.role.ApplicationRole role,
            ApplicationPermissionRule rule,
            ApplicationFeatureType type) {
        return queryResultsCacheProvider.get().execute(this::findByRoleAndRuleAndFeatureType, 
                ApplicationPermissionRepository.class, "findByRoleAndRuleAndFeatureTypeCached", 
                role, rule, type);
    }

    public Collection<ApplicationPermission> findByRoleAndRuleAndFeatureType(
            org.apache.isis.extensions.secman.api.role.ApplicationRole role, 
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByRoleAndRuleAndFeatureType",
                        "role", role,
                        "rule", rule,
                        "featureType", type))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }


    // -- findByRoleAndRuleAndFeature (programmatic)
    public Optional<ApplicationPermission> findByRoleAndRuleAndFeatureCached(
            final org.apache.isis.extensions.secman.api.role.ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type,
            final String featureFqn) {
        return queryResultsCacheProvider.get().execute(
                this::findByRoleAndRuleAndFeature,
                ApplicationPermissionRepository.class, "findByRoleAndRuleAndFeatureCached",
                role, rule, type, featureFqn);
    }

    @Override
    public Optional<ApplicationPermission> findByRoleAndRuleAndFeature(
            final org.apache.isis.extensions.secman.api.role.ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type,
            final String featureFqn) {

        return repository
                .uniqueMatch(
                        new QueryDefault<>(
                                ApplicationPermission.class, "findByRoleAndRuleAndFeature",
                                "role", role,
                                "rule", rule,
                                "featureType", type,
                                "featureFqn", featureFqn ));
    }


    // -- findByFeature (programmatic)

    @Override
    public Collection<ApplicationPermission> findByFeatureCached(final ApplicationFeatureId featureId) {
        return queryResultsCacheProvider.get().execute(
                this::findByFeature, ApplicationPermissionRepository.class, "findByFeatureCached",
                featureId);
    }

    public Collection<ApplicationPermission> findByFeature(final ApplicationFeatureId featureId) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByFeature",
                        "featureType", featureId.getType(),
                        "featureFqn", featureId.getFullyQualifiedName()))
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- newPermission (programmatic)

    @Override
    public ApplicationPermission newPermission(
            final org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final String featureFqn) {

        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);

        final ApplicationFeatureId featureId = ApplicationFeatureId.newFeature(featureType, featureFqn);
        final ApplicationFeature feature = applicationFeatureRepository.findFeature(featureId);
        if(feature == null) {
            messages.warnUser("No such " + featureType.name().toLowerCase() + ": " + featureFqn);
            return null;
        }
        return newPermissionNoCheck(role, rule, mode, featureType, featureFqn);
    }

    public ApplicationPermission newPermissionNoCheck(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final String featureFqn) {

        ApplicationPermission permission = findByRoleAndRuleAndFeature(role, rule, featureType, featureFqn)
                .orElse(null);
        if (permission != null) {
            return permission;
        }
        permission = newApplicationPermission();
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureType(featureType);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);
        return permission;
    }

    @Override
    public ApplicationPermission newPermission(
            final org.apache.isis.extensions.secman.api.role.ApplicationRole genericRole,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String featurePackage,
            final String featureClassName,
            final String featureMemberName) {

        val role = _Casts.<ApplicationRole>uncheckedCast(genericRole);

        val featureId = ApplicationFeatureId.newFeature(featurePackage, featureClassName, featureMemberName);
        val featureType = featureId.getType();
        val featureFqn = featureId.getFullyQualifiedName();

        val feature = applicationFeatureRepository.findFeature(featureId);
        if(feature == null) {
            messages.warnUser("No such " + featureType.name().toLowerCase() + ": " + featureFqn);
            return null;
        }

        val permission = factory.detachedEntity(new ApplicationPermission());
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureType(featureType);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);

        return permission;
    }


    // -- allPermission (programmatic)
    @Override
    public Collection<ApplicationPermission> allPermissions() {
        return repository.allInstances(ApplicationPermission.class)
                .stream()
                .collect(_Sets.toUnmodifiableSorted());
    }

    // -- findOrphaned (programmatic)

    @Override
    public Collection<ApplicationPermission> findOrphaned() {

        final Collection<String> packageNames = applicationFeatureRepository.packageNames();
        final Set<String> availableClasses = _Sets.newTreeSet();
        for (String packageName : packageNames) {
            appendClasses(packageName, ApplicationMemberType.PROPERTY, availableClasses);
            appendClasses(packageName, ApplicationMemberType.COLLECTION, availableClasses);
            appendClasses(packageName, ApplicationMemberType.ACTION, availableClasses);
        }

        val orphaned = _Lists.<ApplicationPermission>newArrayList();

        val permissions = allPermissions();
        for (val permission : permissions) {
            final ApplicationFeatureType featureType = permission.getFeatureType();
            final String featureFqn = permission.getFeatureFqn();

            switch (featureType) {

            case PACKAGE:
                if(!packageNames.contains(featureFqn)) {
                    orphaned.add(permission);
                }
                break;
            case CLASS:
                if(!availableClasses.contains(featureFqn)) {
                    orphaned.add(permission);
                }
                break;
            case MEMBER:

                final List<String> split = _Strings.splitThenStream(featureFqn, "#")
                .collect(Collectors.toList());

                final String fqClassName = split.get(0);
                final String memberName = split.get(1);

                final int lastDot = fqClassName.lastIndexOf('.');
                final String packageName = fqClassName.substring(0, lastDot);
                final String className = fqClassName.substring(lastDot + 1);

                final List<String> memberNames = memberNamesOf(packageName, className);

                if(!memberNames.contains(memberName)) {
                    orphaned.add(permission);
                }
                break;
            }
        }

        return orphaned;
    }

    private void appendClasses(
            final String packageName, final ApplicationMemberType memberType, final Set<String> availableClasses) {
        final Collection<String> classNames = applicationFeatureRepository.classNamesContainedIn(packageName, memberType);
        for (String className : classNames) {
            availableClasses.add(packageName + "." + className);
        }
    }

    private List<String> memberNamesOf(final String packageName, final String className) {
        final List<String> memberNames = _Lists.newArrayList();
        appendMembers(packageName, className, ApplicationMemberType.PROPERTY, memberNames);
        appendMembers(packageName, className, ApplicationMemberType.COLLECTION, memberNames);
        appendMembers(packageName, className, ApplicationMemberType.ACTION, memberNames);
        return memberNames;
    }

    private void appendMembers(
            final String packageName,
            final String className,
            final ApplicationMemberType applicationMemberType,
            final List<String> memberNames) {
        final Collection<String> memberNamesOf =
                applicationFeatureRepository.memberNamesOf(packageName, className, applicationMemberType);
        memberNames.addAll(memberNamesOf);
    }


}
