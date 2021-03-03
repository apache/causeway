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

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureType;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisSecman",
        table = "ApplicationPermission")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByRole", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role"),
    @javax.jdo.annotations.Query(
            name = "findByUser", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE (u.roles.contains(role) && u.username == :username) "
                    + "VARIABLES org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser u"),
    @javax.jdo.annotations.Query(
            name = "findByFeature", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE featureType == :featureType "
                    + "   && featureFqn == :featureFqn"),
    @javax.jdo.annotations.Query(
            name = "findByRoleAndRuleAndFeature", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureType == :featureType "
                    + "   && featureFqn == :featureFqn "),
    @javax.jdo.annotations.Query(
            name = "findByRoleAndRuleAndFeatureType", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureType == :featureType "),
})
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationPermission_role_feature_rule_UNQ", members = { "role", "featureType", "featureFqn", "rule" })
})
@DomainObject(
        objectType = "isis.ext.secman.ApplicationPermission"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
        )
public class ApplicationPermission implements org.apache.isis.extensions.secman.api.permission.ApplicationPermission, Comparable<ApplicationPermission> {

    private static final int TYPICAL_LENGTH_TYPE = 7;  // ApplicationFeatureType.PACKAGE is longest


    // -- role (property)

    public static class RoleDomainEvent extends PropertyDomainEvent<ApplicationRole> {}


    @javax.jdo.annotations.Column(name = "roleId", allowsNull="false")
    @Property(
            domainEvent = RoleDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(hidden = Where.REFERENCES_PARENT)
    @Getter(onMethod = @__(@Override)) 
    private ApplicationRole role;
    
    @Override
    public void setRole(org.apache.isis.extensions.secman.api.role.ApplicationRole applicationRole) {
        role = _Casts.<ApplicationRole>uncheckedCast(applicationRole);
    }

    // -- rule (property)
    public static class RuleDomainEvent extends PropertyDomainEvent<ApplicationPermissionRule> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = RuleDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter(onMethod = @__(@Override)) 
    @Setter(onMethod = @__(@Override))
    private ApplicationPermissionRule rule;


    // -- mode (property)
    public static class ModeDomainEvent extends PropertyDomainEvent<ApplicationPermissionMode> {}


    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = ModeDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter(onMethod = @__(@Override)) 
    @Setter(onMethod = @__(@Override))
    private ApplicationPermissionMode mode;

    // -- featureId (derived property)

    private Optional<ApplicationFeature> getFeature() {
        return createFeatureId()
                .map(featureId -> applicationFeatureRepository.findFeature(featureId));
    }

    // region > type (derived, memberType of associated feature)

    public static class TypeDomainEvent extends PropertyDomainEvent<String> {}

    /**
     * Combines {@link #getFeatureType() feature type} and member type.
     */
    @Property(
            domainEvent = TypeDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(typicalLength=ApplicationPermission.TYPICAL_LENGTH_TYPE)
    public String getType() {
        final Enum<?> e = getFeatureType() != ApplicationFeatureType.MEMBER 
                ? getFeatureType() 
                : getMemberType().orElse(null);
        return e != null ? e.name(): null;
    }

    @Programmatic
    private Optional<ApplicationMemberType> getMemberType() {
        return getFeature()
                .map(ApplicationFeature::getMemberType);
    }


    // -- featureType

    /**
     * The {@link ApplicationFeatureId#getType() feature type} of the
     * feature.
     *
     * <p>
     *     The combination of the feature type and the {@link #getFeatureFqn() fully qualified name} is used to build
     *     the corresponding {@link #getFeature() feature} (view model).
     * </p>
     *
     * @see #getFeatureFqn()
     */
    @javax.jdo.annotations.Column(allowsNull="false")
    @Setter
    private ApplicationFeatureType featureType;

    @Override
    @Programmatic
    public ApplicationFeatureType getFeatureType() {
        return featureType;
    }



    // -- featureFqn

    public static class FeatureFqnDomainEvent extends PropertyDomainEvent<String> {}

    /**
     * The {@link ApplicationFeatureId#getFullyQualifiedName() fully qualified name}
     * of the feature.
     *
     * <p>
     *     The combination of the {@link #getFeatureType() feature type} and the fully qualified name is used to build
     *     the corresponding {@link #getFeature() feature} (view model).
     * </p>
     *
     * @see #getFeatureType()
     */
    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = FeatureFqnDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter @Setter
    private String featureFqn;


    // -- CONTRACT

    private static final ObjectContract<ApplicationPermission> contract	= 
            ObjectContracts.contract(ApplicationPermission.class)
            .thenUse("role", ApplicationPermission::getRole)
            .thenUse("featureType", ApplicationPermission::getFeatureType)
            .thenUse("featureFqn", ApplicationPermission::getFeatureFqn)
            .thenUse("mode", ApplicationPermission::getMode);

    @Override
    public int compareTo(final ApplicationPermission other) {
        return contract.compare(this, other);
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

    // --

    public static class DefaultComparator implements Comparator<ApplicationPermission> {
        @Override
        public int compare(final ApplicationPermission o1, final ApplicationPermission o2) {
            return Objects.compare(o1, o2, (a, b) -> a.compareTo(b) );
        }
    }


    // -- Functions

    @UtilityClass
    public static final class Functions {

        public static final Function<ApplicationPermission, ApplicationPermissionValue> AS_VALUE = 
                (ApplicationPermission input) ->
                    new ApplicationPermissionValue(
                            input.createFeatureId().orElseThrow(_Exceptions::noSuchElement), 
                            input.getRule(), 
                            input.getMode());

    }

    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;

}
