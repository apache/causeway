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
package org.apache.isis.extensions.security.manager.jdo.dom.permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.security.manager.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.security.manager.jdo.dom.user.ApplicationUser;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        repositoryFor = ApplicationPermission.class
)
public class ApplicationPermissionRepository {


    // -- findByRole (programmatic)
    @Programmatic
    public List<ApplicationPermission> findByRoleCached(final ApplicationRole role) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override
            public List<ApplicationPermission> call() throws Exception {
                return findByRole(role);
            }
        }, ApplicationPermissionRepository.class, "findByRoleCached", role);
    }

    @Programmatic
    public List<ApplicationPermission> findByRole(final ApplicationRole role) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByRole",
                        "role", role));
    }
    

    // -- findByUser (programmatic)
    @Programmatic
    public List<ApplicationPermission> findByUserCached(final ApplicationUser user) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByUser(user);
            }
        }, ApplicationPermissionRepository.class, "findByUserCached", user);
    }

    @Programmatic
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
     * multiple lookups from <code>org.apache.isis.extensions.security.manager.jdo.app.user.UserPermissionViewModel</code>.
     */
    @Programmatic
    public ApplicationPermission findByUserAndPermissionValue(final String username, final ApplicationPermissionValue permissionValue) {



        // obtain all permissions for this user, map by its value, and
        // put into query cache (so that this method can be safely called in a tight loop)
        final Map<ApplicationPermissionValue, List<ApplicationPermission>> permissions =
            queryResultsCache.execute(new Callable<Map<ApplicationPermissionValue, List<ApplicationPermission>>>() {
                @Override
                public Map<ApplicationPermissionValue, List<ApplicationPermission>> call() throws Exception {

                    final List<ApplicationPermission> applicationPermissions = findByUser(username);
                    final ImmutableListMultimap<ApplicationPermissionValue, ApplicationPermission> index = Multimaps
                            .index(applicationPermissions, ApplicationPermission.Functions.AS_VALUE);

                    return Multimaps.asMap(index);
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
    @Programmatic
    public List<ApplicationPermission> findByRoleAndRuleAndFeatureTypeCached(
            final ApplicationRole role, final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByRoleAndRuleAndFeatureType(role, rule, type);
            }
        }, ApplicationPermissionRepository.class, "findByRoleAndRuleAndFeatureTypeCached", role, rule, type);
    }

    @Programmatic
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
    @Programmatic
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

    @Programmatic
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
    @Programmatic
    public List<ApplicationPermission> findByFeatureCached(final ApplicationFeatureId featureId) {
        return queryResultsCache.execute(new Callable<List<ApplicationPermission>>() {
            @Override public List<ApplicationPermission> call() throws Exception {
                return findByFeature(featureId);
            }
        }, ApplicationPermissionRepository.class, "findByFeatureCached", featureId);
    }

    @Programmatic
    public List<ApplicationPermission> findByFeature(final ApplicationFeatureId featureId) {
        return repository.allMatches(
                new QueryDefault<>(
                        ApplicationPermission.class, "findByFeature",
                        "featureType", featureId.getType(),
                        "featureFqn", featureId.getFullyQualifiedName()));
    }
    

    // -- newPermission (programmatic)

    @Programmatic
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

    @Programmatic
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
        permission = getApplicationPermissionFactory().newApplicationPermission();
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureType(featureType);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);
        return permission;
    }

    @Programmatic
    public ApplicationPermission newPermission(
            final ApplicationRole role,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String featurePackage,
            final String featureClassName,
            final String featureMemberName) {
        final ApplicationFeatureId featureId = ApplicationFeatureId.newFeature(featurePackage, featureClassName, featureMemberName);
        final ApplicationFeatureType featureType = featureId.getType();
        final String featureFqn = featureId.getFullyQualifiedName();

        final ApplicationFeature feature = applicationFeatureRepository.findFeature(featureId);
        if(feature == null) {
        	messages.warnUser("No such " + featureType.name().toLowerCase() + ": " + featureFqn);
            return null;
        }

        final ApplicationPermission permission = factory.instantiate(ApplicationPermission.class);
        permission.setRole(role);
        permission.setRule(rule);
        permission.setMode(mode);
        permission.setFeatureType(featureType);
        permission.setFeatureFqn(featureFqn);
        repository.persist(permission);

        return permission;
    }
    

    // -- allPermission (programmatic)
    @Programmatic
    public List<ApplicationPermission> allPermissions() {
        return repository.allInstances(ApplicationPermission.class);
    }
    

    // -- findOrphaned (programmatic)

    @Programmatic
    public List<ApplicationPermission> findOrphaned() {

        final Collection<String> packageNames = applicationFeatureRepository.packageNames();
        final Set<String> availableClasses = _Sets.newTreeSet();
        for (String packageName : packageNames) {
            appendClasses(packageName, ApplicationMemberType.PROPERTY, availableClasses);
            appendClasses(packageName, ApplicationMemberType.COLLECTION, availableClasses);
            appendClasses(packageName, ApplicationMemberType.ACTION, availableClasses);
        }

        final List<ApplicationPermission> orphaned = Lists.newArrayList();

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

                final List<String> split = Splitter.on('#').splitToList(featureFqn);
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
        final List<String> memberNames = Lists.newArrayList();
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
    


    //region  >  (injected)
    @Inject
    RepositoryService repository;
    @Inject
    ApplicationFeatureRepositoryDefault applicationFeatureRepository;

    /**
     * Will only be injected to if the programmer has supplied an implementation.  Otherwise
     * this class will install a default implementation in the {@link #getApplicationPermissionFactory() accessor}.
     */
    @Inject
    ApplicationPermissionFactory applicationPermissionFactory;

    private ApplicationPermissionFactory getApplicationPermissionFactory() {
        return applicationPermissionFactory != null
                ? applicationPermissionFactory
                : (applicationPermissionFactory = new ApplicationPermissionFactory.Default(factory));
    }

    @Inject
    QueryResultsCache queryResultsCache;
    
    @javax.inject.Inject
    FactoryService factory;
    
    @javax.inject.Inject
    MessageService messages;
    
    

}
