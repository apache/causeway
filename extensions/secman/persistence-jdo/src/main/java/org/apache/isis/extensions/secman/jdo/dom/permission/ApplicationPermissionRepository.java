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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;

import lombok.val;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationPermission.class
        )
public class ApplicationPermissionRepository 
implements org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository {


    // -- findByRole (programmatic)
    public List<ApplicationPermission> findByRoleCached(final ApplicationRole role) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override
            public List<ApplicationPermission> call() throws Exception {
                return findByRole(role);
            }
        }, ApplicationPermissionRepository.class, "findByRoleCached", role);
    }

    public List<ApplicationPermission> findByRole(final ApplicationRole role) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByRole",
                        "role", role));
    }


    // -- findByUser (programmatic)
    public List<ApplicationPermission> findByUserCached(final ApplicationUser user) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByUser(user);
            }
        }, ApplicationPermissionRepository.class, "findByUserCached", user);
    }

    public List<ApplicationPermission> findByUser(final ApplicationUser user) {
        final String username = user.getUsername();
        return findByUser(username);
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
    public ApplicationPermission findByUserAndPermissionValue(final String username, final ApplicationPermissionValue permissionValue) {

        // obtain all permissions for this user, map by its value, and
        // put into query cache (so that this method can be safely called in a tight loop)
        final Map<ApplicationPermissionValue, List<ApplicationPermission>> permissions =
                queryResultsCache.execute(new Callable<Map<ApplicationPermissionValue, List<ApplicationPermission>>>() {
                    @Override
                    public Map<ApplicationPermissionValue, List<ApplicationPermission>> call() throws Exception {

                        val permissions = findByUser(username);

                        val permissionsByPermissionValue = 
                                _Multimaps.<ApplicationPermissionValue, ApplicationPermission>newListMultimap();

                        _NullSafe.stream(permissions)
                        .forEach(permission->{
                            val permissionValue = ApplicationPermission.Functions.AS_VALUE.apply(permission);
                            permissionsByPermissionValue.putElement(permissionValue, permission);
                        });

                        return permissionsByPermissionValue;
                    }
                    // note: it is correct that only username (and not permissionValue) is the key
                    // (we are obtaining all the perms for this user)
                }, ApplicationPermissionRepository.class, "findByUserAndPermissionValue", username);

        // now simply return the permission from the required value (if it exists)
        final List<ApplicationPermission> applicationPermissions = permissions.get(permissionValue);
        return applicationPermissions != null && !applicationPermissions.isEmpty()
                ? applicationPermissions.get(0)
                        : null;
    }


    // -- findByRoleAndRuleAndFeatureType (programmatic)
    public List<ApplicationPermission> findByRoleAndRuleAndFeatureTypeCached(
            final ApplicationRole role, final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByRoleAndRuleAndFeatureType(role, rule, type);
            }
        }, ApplicationPermissionRepository.class, "findByRoleAndRuleAndFeatureTypeCached", role, rule, type);
    }

    public List<ApplicationPermission> findByRoleAndRuleAndFeatureType(
            final ApplicationRole role, final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByRoleAndRuleAndFeatureType",
                        "role", role,
                        "rule", rule,
                        "featureType", type));
    }


    // -- findByRoleAndRuleAndFeature (programmatic)
    public ApplicationPermission findByRoleAndRuleAndFeatureCached(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type,
            final String featureFqn) {
        return queryResultsCache.execute(new Callable<ApplicationPermission>() {
            @Override public ApplicationPermission call() throws Exception {
                return findByRoleAndRuleAndFeature(role, rule, type, featureFqn);
            }
        }, ApplicationPermissionRepository.class, "findByRoleAndRuleAndFeatureCached", role, rule, type, featureFqn);
    }

    public ApplicationPermission findByRoleAndRuleAndFeature(
            final ApplicationRole role,
            final ApplicationPermissionRule rule, final ApplicationFeatureType type, final String featureFqn) {
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
    public List<ApplicationPermission> findByFeatureCached(final ApplicationFeatureId featureId) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByFeature(featureId);
            }
        }, ApplicationPermissionRepository.class, "findByFeatureCached", featureId);
    }

    public List<ApplicationPermission> findByFeature(final ApplicationFeatureId featureId) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByFeature",
                        "featureType", featureId.getType(),
                        "featureFqn", featureId.getFullyQualifiedName()));
    }


    // -- newPermission (programmatic)

    public ApplicationPermission newPermission(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final ApplicationFeatureType featureType,
            final String featureFqn) {
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

        ApplicationPermission permission = findByRoleAndRuleAndFeature(role, rule, featureType, featureFqn);
        if (permission != null) {
            return permission;
        }
        permission = applicationPermissionFactory.newApplicationPermission();
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureType(featureType);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);
        return permission;
    }

    public ApplicationPermission newPermission(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String featurePackage,
            final String featureClassName,
            final String featureMemberName) {

        val featureId = ApplicationFeatureId.newFeature(featurePackage, featureClassName, featureMemberName);
        val featureType = featureId.getType();
        val featureFqn = featureId.getFullyQualifiedName();

        val feature = applicationFeatureRepository.findFeature(featureId);
        if(feature == null) {
            messages.warnUser("No such " + featureType.name().toLowerCase() + ": " + featureFqn);
            return null;
        }

        val permission = factory.instantiate(ApplicationPermission.class);
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
    public List<ApplicationPermission> allPermissions() {
        return repository.allInstances(ApplicationPermission.class);
    }


    // -- findOrphaned (programmatic)

    @Override
    public List<ApplicationPermission> findOrphaned() {

        final Collection<String> packageNames = applicationFeatureRepository.packageNames();
        final Set<String> availableClasses = _Sets.newTreeSet();
        for (String packageName : packageNames) {
            appendClasses(packageName, ApplicationMemberType.PROPERTY, availableClasses);
            appendClasses(packageName, ApplicationMemberType.COLLECTION, availableClasses);
            appendClasses(packageName, ApplicationMemberType.ACTION, availableClasses);
        }

        final List<ApplicationPermission> orphaned = _Lists.newArrayList();

        final List<ApplicationPermission> permissions = allPermissions();
        for (ApplicationPermission permission : permissions) {
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

    // -- DEPENDENCIES

    @Inject RepositoryService repository;
    @Inject ApplicationFeatureRepositoryDefault applicationFeatureRepository;
    @Inject ApplicationPermissionFactory applicationPermissionFactory;
    @Inject QueryResultsCache queryResultsCache;
    @Inject FactoryService factory;
    @Inject MessageService messages;

}
