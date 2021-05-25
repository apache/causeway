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
package org.apache.isis.extensions.secman.api.permission.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Specifies how a particular {@link #getRole() application role} may interact with a specific
 * {@link ApplicationFeature application feature}.
 *
 * <p>
 *     Each permission has a {@link #getRule() rule} and a {@link #getMode() mode}.  The
 *     {@link ApplicationPermissionRule rule} determines whether the permission
 *     {@link ApplicationPermissionRule#ALLOW grants}
 *     access to the feature or {@link ApplicationPermissionRule#VETO veto}es access
 *     to it.  The {@link ApplicationPermissionMode mode} indicates whether
 *     the role can {@link ApplicationPermissionMode#VIEWING view} the feature
 *     or can {@link ApplicationPermissionMode#CHANGING change} the state of the
 *     system using the feature.
 * </p>
 *
 * <p>
 *     For a given permission, there is an interaction between the {@link ApplicationPermissionRule rule} and the
 *     {@link ApplicationPermissionMode mode}:
 * <ul>
 *     <li>for an {@link ApplicationPermissionRule#ALLOW allow}, a
 *     {@link ApplicationPermissionMode#CHANGING usability} allow
 *     implies {@link ApplicationPermissionMode#VIEWING visibility} allow.
 *     </li>
 *     <li>conversely, for a {@link ApplicationPermissionRule#VETO veto},
 *     a {@link ApplicationPermissionMode#VIEWING visibility} veto
 *     implies a {@link ApplicationPermissionMode#CHANGING usability} veto.</li>
 * </ul>
 * </p>
 *
 * @since 2.0 {@index}
 */
@DomainObject(
        objectType = ApplicationPermission.OBJECT_TYPE
)
public abstract class ApplicationPermission implements Comparable<ApplicationPermission> {

    public static final String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationPermission";

    public static final String NAMED_QUERY_FIND_BY_FEATURE = "ApplicationPermission.findByFeature";
    public static final String NAMED_QUERY_FIND_BY_ROLE = "ApplicationPermission.findByRole";
    public static final String NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE = "ApplicationPermission.findByRoleAndRuleAndFeature";
    public static final String NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE_FQN = "ApplicationPermission.findByRoleAndRuleAndFeatureAndFqn";
    public static final String NAMED_QUERY_FIND_BY_USER = "ApplicationPermission.findByUser";


    @Inject transient ApplicationFeatureRepository featureRepository;

    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermission, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermission, T> {}


    // -- MODEL

    public String title() {
        val buf = new StringBuilder();
        buf.append(getRole().getName()).append(":")  // admin:
        .append(" ").append(getRule().toString()) // Allow|Veto
        .append(" ").append(getMode().toString()) // Viewing|Changing
        .append(" of ");

        asFeatureId()
        .ifPresent(featureId->{

            switch (featureId.getSort()) {
            case NAMESPACE:
                buf.append(getFeatureFqn());              // com.mycompany
                break;
            case TYPE:
                // abbreviate if required because otherwise title overflows on action prompt.
                if(getFeatureFqn().length() < 30) {
                    buf.append(getFeatureFqn());          // com.mycompany.Bar
                } else {
                    buf.append(featureId.getTypeSimpleName()); // Bar
                }
                break;
            case MEMBER:
                buf.append(featureId.getTypeSimpleName())
                .append("#")
                .append(featureId.getLogicalMemberName());   // com.mycompany.Bar#foo
                break;
            }

        });

        return buf.toString();
    }



    // -- ROLE

    @Property(
            domainEvent = Role.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "identity",
            hidden = Where.REFERENCES_PARENT,
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Role {
        public static class DomainEvent extends PropertyDomainEvent<ApplicationRole> {}
    }

    @Role
    public abstract ApplicationRole getRole();
    public abstract void setRole(ApplicationRole applicationRole);


    // -- RULE

    @Property(
            domainEvent = Rule.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "rule",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Rule {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermissionRule> {}
    }

    @Rule
    public abstract ApplicationPermissionRule getRule();
    public abstract void setRule(ApplicationPermissionRule rule);


    // -- MODE

    @Property(
            domainEvent = Mode.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "mode",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Mode {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermissionMode> {}
    }

    @Mode
    public abstract ApplicationPermissionMode getMode();
    public abstract void setMode(ApplicationPermissionMode mode);


    // -- SORT

    /**
     * Combines {@link #getFeatureSort() feature type} and member type.
     */
    @Property(
            domainEvent = Sort.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "3",
            typicalLength= Sort.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sort {
        int TYPICAL_LENGTH = 7;  // ApplicationFeatureType.PACKAGE is longest

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Sort
    public String getSort() {
        final Enum<?> e = getFeatureSort() != ApplicationFeatureSort.MEMBER
                ? getFeatureSort()
                : getMemberSort().orElse(null);
        return e != null ? e.name(): null;
    }


    // -- FEATURE SORT

    /**
     * Which {@link ApplicationFeatureId#getSort() sort} of
     * feature this is.
     *
     * <p>
     *     The combination of the feature type and the {@link #getFeatureFqn() fully qualified name} is used to build
     *     the corresponding {@link #getFeature() feature} (view model).
     * </p>
     *
     * @see #getFeatureFqn()
     */
    @Programmatic
    public abstract ApplicationFeatureSort getFeatureSort();
    public abstract void setFeatureSort(ApplicationFeatureSort featureSort);


    // -- FQN

    /**
     * The {@link ApplicationFeatureId#getFullyQualifiedName() fully qualified name}
     * of the feature.
     *
     * <p>
     *     The combination of the {@link #getFeatureSort() feature type} and the fully qualified name is used to build
     *     the corresponding feature view model.
     * </p>
     *
     * @see #getFeatureSort()
     */
    @Property(
            domainEvent = FeatureFqn.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FeatureFqn {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FeatureFqn
    public abstract String getFeatureFqn();
    public abstract void setFeatureFqn(String featureFqn);


    // -- FIND FEATURE

    @Programmatic
    public ApplicationFeature findFeature(ApplicationFeatureId featureId) {
        return featureRepository.findFeature(featureId);
    }

    private Optional<ApplicationMemberSort> getMemberSort() {
        return getFeature()
                .flatMap(ApplicationFeature::getMemberSort);
    }

    private Optional<ApplicationFeature> getFeature() {
        return asFeatureId()
                .map(this::findFeature);
    }


    // -- HELPER

    @Programmatic
    Optional<ApplicationFeatureId> asFeatureId() {
        return Optional.ofNullable(getFeatureSort())
                .map(featureSort -> ApplicationFeatureId.newFeature(featureSort, getFeatureFqn()));
    }


    // -- CONTRACT

    private static final ObjectContracts.ObjectContract<ApplicationPermission> contract	=
            ObjectContracts.contract(ApplicationPermission.class)
                    .thenUse("role", ApplicationPermission::getRole)
                    .thenUse("featureSort", ApplicationPermission::getFeatureSort)
                    .thenUse("featureFqn", ApplicationPermission::getFeatureFqn)
                    .thenUse("mode", ApplicationPermission::getMode);

    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission other) {
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


    public static class DefaultComparator implements Comparator<ApplicationPermission> {
        @Override
        public int compare(final ApplicationPermission o1, final ApplicationPermission o2) {
            return Objects.compare(o1, o2, Comparator.naturalOrder());
        }
    }

    @UtilityClass
    public static final class Functions {
        public static final Function<ApplicationPermission, ApplicationPermissionValue> AS_VALUE =
                input -> new ApplicationPermissionValue(
                        input.asFeatureId().orElseThrow(_Exceptions::noSuchElement),
                        input.getRule(),
                        input.getMode());
    }


}
