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
package org.apache.isis.extensions.secman.jpa.permission.dom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationPermission",
        uniqueConstraints=
            @UniqueConstraint(
                    name = "ApplicationPermission_role_feature_rule_UNQ",
                    columnNames={"roleId", "featureSort", "featureFqn", "rule"})
)
@NamedQueries({
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_USER,
            //TODO this query returns empty result
            query = "SELECT p "
                  + "FROM ApplicationPermission p "
                  + "   , ApplicationUser u "
                  + "WHERE u.username = :username"
                  + "  AND p.role MEMBER OF u.roles"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_FEATURE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.featureSort = :featureSort "
                  + "   AND p.featureFqn = :featureFqn"),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE_FQN,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "
                  + "   AND p.featureFqn = :featureFqn "),
    @NamedQuery(
            name = org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission.NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "),
})
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
        objectType = ApplicationPermission.OBJECT_TYPE
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
    extends org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission {


    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;


    // -- ROLE

    @ManyToOne
    @JoinColumn(name="roleId", nullable=false)
    private org.apache.isis.extensions.secman.jpa.role.dom.ApplicationRole role;

    @Role
    @Override
    public ApplicationRole getRole() {
        return role;
    }
    @Override
    public void setRole(ApplicationRole applicationRole) {
        role = _Casts.uncheckedCast(applicationRole);
    }


    // -- RULE

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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

    @Column(nullable = false)
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


}
