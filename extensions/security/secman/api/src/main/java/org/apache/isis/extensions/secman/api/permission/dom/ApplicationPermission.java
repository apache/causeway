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

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
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
@DomainObject(objectType = "isis.ext.secman.IApplicationPermission")
public interface ApplicationPermission {

    String NAMED_QUERY_FIND_BY_FEATURE = "ApplicationPermission.findByFeature";
    String NAMED_QUERY_FIND_BY_ROLE = "ApplicationPermission.findByRole";
    String NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE = "ApplicationPermission.findByRoleAndRuleAndFeature";
    String NAMED_QUERY_FIND_BY_ROLE_RULE_FEATURE_FQN = "ApplicationPermission.findByRoleAndRuleAndFeatureAndFqn";
    String NAMED_QUERY_FIND_BY_USER = "ApplicationPermission.findByUser";


    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermission, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermission, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermission> {}

    public static class AllowDomainEvent extends ActionDomainEvent {}
    public static class UpdateRoleDomainEvent extends ActionDomainEvent {}
    public static class VetoDomainEvent extends ActionDomainEvent {}
    public static class DeleteDomainEvent extends ActionDomainEvent {}
    public static class ChangingDomainEvent extends ActionDomainEvent {}
    public static class ViewingDomainEvent extends ActionDomainEvent {}

    public static class RelocateNamespaceDomainEvent extends ActionDomainEvent {}

    // -- MODEL

    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    default String title() {
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
                .append(featureId.getMemberName());   // com.mycompany.Bar#foo
                break;
            }

        });

        return buf.toString();
    }

    ApplicationFeatureSort getFeatureSort();
    void setFeatureSort(ApplicationFeatureSort featureSort);

    // -- ROLE

    @Property
    @PropertyLayout(
            hidden=Where.REFERENCES_PARENT,
            fieldSetId="Role",
            sequence = "1")
    default ApplicationRole getRole() {
        throw _Exceptions.unsupportedOperation("please implement me");
    }
    void setRole(ApplicationRole applicationRole);

    // -- RULE

    @Property
    @PropertyLayout(fieldSetId="Permissions", sequence = "2")
    default ApplicationPermissionRule getRule() {
        throw _Exceptions.unsupportedOperation("please implement me");
    }
    void setRule(ApplicationPermissionRule rule);

    // -- MODE

    @Property
    @PropertyLayout(fieldSetId="Permissions", sequence = "3")
    default ApplicationPermissionMode getMode() {
        throw _Exceptions.unsupportedOperation("please implement me");
    }
    void setMode(ApplicationPermissionMode changing);

    // -- SORT

    @Property
    @PropertyLayout(fieldSetId="Feature", sequence = "5")
    default String getSort() {
        throw _Exceptions.unsupportedOperation("please implement me");
    }

    // -- FQN

    @Property
    @PropertyLayout(fieldSetId="Feature", sequence = "5.1")
    default String getFeatureFqn() {
        throw _Exceptions.unsupportedOperation("please implement me");
    }
    void setFeatureFqn(String featureFqn);

    // -- HELPER

    @Programmatic
    default Optional<ApplicationFeatureId> asFeatureId() {
        return Optional.ofNullable(getFeatureSort())
                .map(featureSort -> ApplicationFeatureId.newFeature(featureSort, getFeatureFqn()));
    }

    @UtilityClass
    public static final class Functions {
        public static final Function<ApplicationPermission, ApplicationPermissionValue> AS_VALUE =
                new Function<ApplicationPermission, ApplicationPermissionValue>() {
                    @Override
                    public ApplicationPermissionValue apply(ApplicationPermission input) {
                        return new ApplicationPermissionValue(
                                input.asFeatureId().orElseThrow(_Exceptions::noSuchElement),
                                input.getRule(),
                                input.getMode());
                    }
                };
    }


}
