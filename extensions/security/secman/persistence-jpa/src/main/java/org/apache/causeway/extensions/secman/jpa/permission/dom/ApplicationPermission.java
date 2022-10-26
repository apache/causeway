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
package org.apache.causeway.extensions.secman.jpa.permission.dom;

import javax.inject.Named;
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission.Nq;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        schema = ApplicationPermission.SCHEMA,
        name = ApplicationPermission.TABLE,
        uniqueConstraints=
            @UniqueConstraint(name = "ApplicationPermission_role_feature_rule__UNQ", columnNames={"roleId", "featureSort", "featureFqn", "rule"})
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND_BY_ROLE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role"),
    @NamedQuery(
            name = Nq.FIND_BY_USER,
            query = "SELECT perm "
                  + "  FROM ApplicationPermission perm "
                  + "  JOIN perm.role role "
                  + "  JOIN role.users user "
                  + " WHERE user.username = :username"),
    @NamedQuery(
            name = Nq.FIND_BY_ROLE_NAMES,
            query = "SELECT perm "
                  + "  FROM ApplicationPermission perm "
                  + "  JOIN perm.role role "
                  + " WHERE role.name IN :roleNames"),
    @NamedQuery(
            name = Nq.FIND_BY_FEATURE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.featureSort = :featureSort "
                  + "   AND p.featureFqn = :featureFqn"),
    @NamedQuery(
            name = Nq.FIND_BY_ROLE_RULE_FEATURE_FQN,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "
                  + "   AND p.featureFqn = :featureFqn "),
    @NamedQuery(
            name = Nq.FIND_BY_ROLE_RULE_FEATURE,
            query = "SELECT p "
                  + "  FROM ApplicationPermission p "
                  + " WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "),
})
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
@Named(ApplicationPermission.LOGICAL_TYPE_NAME)
@DomainObject
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
    extends org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission {


    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;


    @ManyToOne
    @JoinColumn(name=Role.NAME, nullable=Role.NULLABLE)
    @Role
    @Getter
    private org.apache.causeway.extensions.secman.jpa.role.dom.ApplicationRole role;
    @Override
    public void setRole(final ApplicationRole applicationRole) {
        role = _Casts.uncheckedCast(applicationRole);
    }


    @Column(nullable = Rule.NULLABLE)
    @Enumerated(EnumType.STRING)
    @Rule
    @Getter @Setter
    private ApplicationPermissionRule rule;


    @Column(nullable = Mode.NULLABLE) @Enumerated(EnumType.STRING)
    @Mode
    @Getter @Setter
    private ApplicationPermissionMode mode;


    @Column(nullable = FeatureSort.NULLABLE) @Enumerated(EnumType.STRING)
    @FeatureSort
    @Getter @Setter
    private ApplicationFeatureSort featureSort;


    @Column(nullable = FeatureFqn.NULLABLE)
    @FeatureFqn
    @Getter @Setter
    private String featureFqn;


}
