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
package org.apache.causeway.extensions.secman.jdo.permission.dom;

import javax.inject.Named;
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

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = ApplicationPermission.SCHEMA,
        table = ApplicationPermission.TABLE)
@Uniques({
        @Unique(name = "ApplicationPermission__role_feature_rule__UNQ", members = { "role", "featureSort", "featureFqn", "rule" })
})
@Queries( {
    @Query(
            name = Nq.FIND_BY_ROLE,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE role == :role"),
    @Query(
            name = Nq.FIND_BY_USER,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE (u.roles.contains(role) && u.username == :username) "
                  + " VARIABLES org.apache.causeway.extensions.secman.jdo.user.dom.ApplicationUser u"),
    @Query(
            name = Nq.FIND_BY_ROLE_NAMES,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE :roleNames.contains(role.name) "),
    @Query(
            name = Nq.FIND_BY_FEATURE,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE featureSort == :featureSort "
                  + "    && featureFqn == :featureFqn"),
    @Query(
            name = Nq.FIND_BY_ROLE_RULE_FEATURE_FQN,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE role == :role "
                  + "    && rule == :rule "
                  + "    && featureSort == :featureSort "
                  + "    && featureFqn == :featureFqn "),
    @Query(
            name = Nq.FIND_BY_ROLE_RULE_FEATURE,
            value = "SELECT "
                  + "  FROM " + ApplicationPermission.FQCN
                  + " WHERE role == :role "
                  + "    && rule == :rule "
                  + "    && featureSort == :featureSort "),
})
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE, column = "id")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "version")
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@Named(ApplicationPermission.LOGICAL_TYPE_NAME)
@DomainObject()
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
    extends org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission {

    protected final static String FQCN = "org.apache.causeway.extensions.secman.jdo.permission.dom.ApplicationPermission";


    @Column(name = Role.NAME, allowsNull = Role.ALLOWS_NULL)
    @Role
    @Getter
    private org.apache.causeway.extensions.secman.jdo.role.dom.ApplicationRole role;
    @Override
    public void setRole(final ApplicationRole role) {
        this.role = _Casts.uncheckedCast(role);
    }


    @Column(allowsNull = Rule.ALLOWS_NULL)
    @Rule
    @Getter @Setter
    private ApplicationPermissionRule rule;


    @Column(allowsNull = Mode.ALLOWS_NULL)
    @Mode
    @Getter @Setter
    private ApplicationPermissionMode mode;


    @Column(allowsNull = FeatureSort.ALLOWS_NULL)
    @FeatureSort
    @Getter @Setter
    private ApplicationFeatureSort featureSort;


    @Column(allowsNull = FeatureFqn.ALLOWS_NULL)
    @FeatureFqn
    @Getter @Setter
    private String featureFqn;


}
