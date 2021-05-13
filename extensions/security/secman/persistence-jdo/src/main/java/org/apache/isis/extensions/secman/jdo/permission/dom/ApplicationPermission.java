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
package org.apache.isis.extensions.secman.jdo.permission.dom;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationPermission")
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Queries( {
    @Query(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE,
            value = "SELECT "
                    + "FROM " + ApplicationPermission.FQCN
                    + "WHERE role == :role"),
    @Query(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_USER,
            value = "SELECT "
                    + "FROM " + ApplicationPermission.FQCN
                    + "WHERE (u.roles.contains(role) && u.username == :username) "
                    + "VARIABLES org.apache.isis.extensions.secman.jdo.user.dom.ApplicationUser u"),
    @Query(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_FEATURE,
            value = "SELECT "
                    + "FROM " + ApplicationPermission.FQCN
                    + "WHERE featureSort == :featureSort "
                    + "   && featureFqn == :featureFqn"),
    @Query(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE_FQN,
            value = "SELECT "
                    + "FROM " + ApplicationPermission.FQCN
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureSort == :featureSort "
                    + "   && featureFqn == :featureFqn "),
    @Query(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE,
            value = "SELECT "
                    + "FROM " + ApplicationPermission.FQCN
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureSort == :featureSort "),
})
@Uniques({
    @Unique(
            name = "ApplicationPermission_role_feature_rule_UNQ",
            members = { "role", "featureSort", "featureFqn", "rule" })
})
@DomainObject(
        objectType = ApplicationPermission.OBJECT_TYPE
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
    implements org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission {

    protected final static String FQCN = "org.apache.isis.extensions.secman.jdo.permission.dom.ApplicationPermission";

    @Inject ApplicationFeatureRepository featureRepository;


    // -- ROLE

    @Column(name = "roleId", allowsNull = "false")
    private org.apache.isis.extensions.secman.jdo.role.dom.ApplicationRole role;

    @Role
    @Override
    public ApplicationRole getRole() {
        return role;
    }
    @Override
    public void setRole(ApplicationRole role) {
        this.role = _Casts.uncheckedCast(role);
    }


    // -- RULE

    @Column(allowsNull = "false")
    private ApplicationPermissionRule rule;

    @Override
    @Rule
    public ApplicationPermissionRule getRule() {
        return rule;
    }
    @Override
    public void setRule(ApplicationPermissionRule rule) {
        this.rule = rule;
    }


    // -- MODE

    @Column(allowsNull = "false")
    private ApplicationPermissionMode mode;

    @Mode
    @Override
    public ApplicationPermissionMode getMode() {
        return mode;
    }
    @Override
    public void setMode(ApplicationPermissionMode mode) {
        this.mode = mode;
    }




    // -- FEATURE SORT

    @Column(allowsNull = "false")
    private ApplicationFeatureSort featureSort;

    @Programmatic
    @Override
    public ApplicationFeatureSort getFeatureSort() {
        return featureSort;
    }
    @Override
    public void setFeatureSort(ApplicationFeatureSort featureSort) {
        this.featureSort = featureSort;
    }


    // -- FQN

    @Column(allowsNull = "false")
    private String featureFqn;

    @FeatureFqn
    @Override
    public String getFeatureFqn() {
        return featureFqn;
    }
    @Override
    public void setFeatureFqn(String featureFqn) {
        this.featureFqn = featureFqn;
    }


    // FIND FEATURE

    @Override
    @Programmatic
    public ApplicationFeature findFeature(ApplicationFeatureId featureId) {
        return featureRepository.findFeature(featureId);
    }


    // -- CONTRACT

    private static final ObjectContract<ApplicationPermission> contract	=
            ObjectContracts.contract(ApplicationPermission.class)
            .thenUse("role", ApplicationPermission::getRole)
            .thenUse("featureSort", ApplicationPermission::getFeatureSort)
            .thenUse("featureFqn", ApplicationPermission::getFeatureFqn)
            .thenUse("mode", ApplicationPermission::getMode);

    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission other) {
        return contract.compare(this, (ApplicationPermission)other);
    }

    @Override
    public boolean equals(final Object other) {
        return contract.equals(this, other);
    }

    @Override
    public int hashCode() {
        return contract.hashCode(this);
    }

    @Override
    public String toString() {
        return contract.toString(this);
    }

}
