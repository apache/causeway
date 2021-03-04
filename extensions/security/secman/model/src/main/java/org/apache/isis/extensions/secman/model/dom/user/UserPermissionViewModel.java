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
package org.apache.isis.extensions.secman.model.dom.user;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.model.dom.features.ApplicationFeatureViewModel;

import lombok.val;

/**
 * View model identified by {@link ApplicationFeatureId} and backed by an
 * {@link ApplicationFeature}.
 */
@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "isis.ext.secman.UserPermissionViewModel"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )

public class UserPermissionViewModel implements ViewModel {

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<UserPermissionViewModel, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<UserPermissionViewModel, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<UserPermissionViewModel> {}

    private static final int TYPICAL_LENGTH_VERB = 12;
    
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;

    // -- constructors, factory methods
    public static UserPermissionViewModel newViewModel(
            final ApplicationFeatureId featureId, 
            final ApplicationUser user, 
            final ApplicationPermissionValueSet.Evaluation viewingEvaluation, 
            final ApplicationPermissionValueSet.Evaluation changingEvaluation, 
            final FactoryService factory) {

        return factory
                .viewModel(
                        UserPermissionViewModel.class, 
                        asEncodedString(featureId, user.getUsername(), viewingEvaluation, changingEvaluation));
    }

    public UserPermissionViewModel() {
        setFeatureId(ApplicationFeatureId.NAMESPACE_DEFAULT);
    }


    // -- identification
    public String title() {
        return getVerb() + " " + getFeatureId().getFullyQualifiedName();
    }

    public String iconName() {
        return "userPermission";
    }

    // -- ViewModel impl
    @Override
    public String viewModelMemento() {
        return asEncodedString();
    }

    @Override
    public void viewModelInit(final String encodedMemento) {
        parseEncoded(encodedMemento);
    }

    private static String asEncodedString(
            final ApplicationFeatureId featureId,
            final String username,
            final ApplicationPermissionValueSet.Evaluation viewingEvaluation,
            final ApplicationPermissionValueSet.Evaluation changingEvaluation) {
        return base64UrlEncode(asString(featureId, username, viewingEvaluation, changingEvaluation));
    }

    private static String asString(
            final ApplicationFeatureId featureId,
            final String username,
            final ApplicationPermissionValueSet.Evaluation viewingEvaluation,
            final ApplicationPermissionValueSet.Evaluation changingEvaluation) {

        final boolean viewingEvaluationGranted = viewingEvaluation.isGranted();
        final ApplicationPermissionValue viewingEvaluationCause = viewingEvaluation.getCause();
        final ApplicationFeatureId viewingEvaluationCauseFeatureId = viewingEvaluationCause != null? viewingEvaluationCause.getFeatureId(): null;

        final boolean changingEvaluationGranted = changingEvaluation.isGranted();
        final ApplicationPermissionValue changingEvaluationCause = changingEvaluation.getCause();
        final ApplicationFeatureId changingEvaluationCauseFeatureId = changingEvaluationCause != null? changingEvaluationCause.getFeatureId(): null;

        return join(
        username, 
        
        viewingEvaluationGranted,
        viewingEvaluationCauseFeatureId != null? viewingEvaluationCauseFeatureId.getSort(): "",
        viewingEvaluationCauseFeatureId != null? viewingEvaluationCauseFeatureId.getFullyQualifiedName(): "",
        viewingEvaluationCause != null? viewingEvaluationCause.getRule(): "",
        viewingEvaluationCause != null? viewingEvaluationCause.getMode(): "",
        
        changingEvaluationGranted,
        changingEvaluationCauseFeatureId != null? changingEvaluationCauseFeatureId.getSort(): "",
        changingEvaluationCauseFeatureId != null? changingEvaluationCauseFeatureId.getFullyQualifiedName(): "",
        changingEvaluationCause != null? changingEvaluationCause.getRule(): "",
        changingEvaluationCause != null? changingEvaluationCause.getMode(): "",
        
        featureId.getSort(), 
        featureId.getFullyQualifiedName()
        );
    }

    private static String join(Object ... args) {
        return _NullSafe.stream(args)
                .map(arg->""+arg)
                .collect(Collectors.joining(":"));
    }

    private void parseEncoded(final String encodedString) {
        parse(base64UrlDecode(encodedString));
    }

    private void parse(final String asString) {
        final Iterator<String> iterator = _Strings.splitThenStream(asString, ":")
                .collect(Collectors.toList())
                .listIterator();

        this.username = iterator.next();

        this.viewingGranted = Boolean.valueOf(iterator.next());
        final String viewingEvaluationCauseFeatureIdType = iterator.next();
        final ApplicationFeatureSort viewingEvaluationFeatureIdType =  !viewingEvaluationCauseFeatureIdType.isEmpty() ? ApplicationFeatureSort.valueOf(viewingEvaluationCauseFeatureIdType) : null;
        final String viewingEvaluationFeatureFqn = iterator.next();
        this.viewingFeatureId = viewingEvaluationFeatureIdType != null
                ? ApplicationFeatureId.newFeature(viewingEvaluationFeatureIdType, viewingEvaluationFeatureFqn) 
                : null;

        final String viewingEvaluationCauseRule = iterator.next();
        this.viewingRule = !viewingEvaluationCauseRule.isEmpty()? ApplicationPermissionRule.valueOf(viewingEvaluationCauseRule): null;
        final String viewingEvaluationCauseMode = iterator.next();
        this.viewingMode = !viewingEvaluationCauseMode.isEmpty()? ApplicationPermissionMode.valueOf(viewingEvaluationCauseMode): null;


        this.changingGranted = Boolean.valueOf(iterator.next());
        final String changingEvaluationCauseFeatureIdType = iterator.next();
        final ApplicationFeatureSort changingEvaluationFeatureIdType =  !changingEvaluationCauseFeatureIdType.isEmpty() ? ApplicationFeatureSort.valueOf(changingEvaluationCauseFeatureIdType) : null;
        final String changingEvaluationFeatureFqn = iterator.next();
        this.changingFeatureId = changingEvaluationFeatureIdType != null
                ? ApplicationFeatureId.newFeature(changingEvaluationFeatureIdType, changingEvaluationFeatureFqn) 
                : null;

        final String changingEvaluationCauseRule = iterator.next();
        this.changingRule = !changingEvaluationCauseRule.isEmpty()? ApplicationPermissionRule.valueOf(changingEvaluationCauseRule): null;
        final String changingEvaluationCauseMode = iterator.next();
        this.changingMode = !changingEvaluationCauseMode.isEmpty()? ApplicationPermissionMode.valueOf(changingEvaluationCauseMode): null;

        final ApplicationFeatureSort type = ApplicationFeatureSort.valueOf(iterator.next());
        this.featureId = ApplicationFeatureId.newFeature(type, iterator.next());
    }


    @Programmatic
    public String asEncodedString() {
        return asEncodedString(getFeatureId(), getUsername(), newEvaluation(viewingGranted, viewingFeatureId, viewingRule, viewingMode), newEvaluation(changingGranted, changingFeatureId, changingRule, changingMode));
    }

    private static ApplicationPermissionValueSet.Evaluation newEvaluation(final boolean granted, final ApplicationFeatureId featureId, final ApplicationPermissionRule rule, final ApplicationPermissionMode mode) {
        return new ApplicationPermissionValueSet.Evaluation(newPermissionValue(featureId, rule, mode), granted);
    }

    private static ApplicationPermissionValue newPermissionValue(final ApplicationFeatureId featureId, final ApplicationPermissionRule rule, final ApplicationPermissionMode mode) {
        if(featureId == null || mode == null || rule == null) {
            return null;
        } else {
            return new ApplicationPermissionValue(featureId, rule, mode);
        }
    }

    private static String base64UrlDecode(final String str) {
        val bytes = Base64.getUrlDecoder().decode(str);
        return _Strings.ofBytes(bytes, StandardCharsets.UTF_8);
    }

    private static String base64UrlEncode(final String str) {
        val bytes = str.getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().encodeToString(bytes);
    }


    // -- user (derived property, hidden in parented tables)
    public static class UserDomainEvent extends PropertyDomainEvent<ApplicationUser> {}

    @Property(
            domainEvent = UserDomainEvent.class
            )
    @PropertyLayout(hidden=Where.PARENTED_TABLES)
    @MemberOrder(name = "Permission", sequence = "1")
    public ApplicationUser getUser() {
        return applicationUserRepository.findOrCreateUserByUsername(getUsername());
    }

    private String username;
    @Programmatic
    public String getUsername() {
        return username;
    }




    // -- verb (derived property)

    public static class VerbDomainEvent extends PropertyDomainEvent<String> {}

    private boolean viewingGranted;
    private boolean changingGranted;

    @Property(
            domainEvent = VerbDomainEvent.class
            )
    @PropertyLayout(typicalLength=UserPermissionViewModel.TYPICAL_LENGTH_VERB)
    @MemberOrder(name="Permission", sequence = "2")
    public String getVerb() {
        return changingGranted
                ? "Can change"
                        : viewingGranted
                        ? "Can view"
                                : "No access to";
    }


    // -- feature (derived property)

    public static class FeatureDomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel> {}

    @Property(
            domainEvent = FeatureDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(hidden=Where.REFERENCES_PARENT)
    @MemberOrder(name = "Permission",sequence = "4")
    public ApplicationFeatureViewModel getFeature() {
        if(getFeatureId() == null) {
            return null;
        }
        return ApplicationFeatureViewModel.newViewModel(getFeatureId(), applicationFeatureRepository, factory);
    }


    private ApplicationFeatureId featureId;

    @Programmatic
    public ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    public void setFeatureId(final ApplicationFeatureId applicationFeatureId) {
        this.featureId = applicationFeatureId;
    }


    // -- viewingPermission (derived property)

    public static class ViewingPermissionDomainEvent extends PropertyDomainEvent<ApplicationPermission> {}

    private ApplicationFeatureId viewingFeatureId;
    private ApplicationPermissionMode viewingMode;
    private ApplicationPermissionRule viewingRule;

    @Property(
            domainEvent = ViewingPermissionDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(hidden=Where.REFERENCES_PARENT)
    @MemberOrder(name="Cause", sequence = "2.1")
    public ApplicationPermission getViewingPermission() {
        if(getViewingPermissionValue() == null) {
            return null;
        }
        return applicationPermissionRepository.findByUserAndPermissionValue(username, getViewingPermissionValue())
                .orElse(null);
    }

    private ApplicationPermissionValue getViewingPermissionValue() {
        if(viewingFeatureId == null) {
            return null;
        }
        return new ApplicationPermissionValue(viewingFeatureId, viewingRule, viewingMode);
    }


    // -- changingPermission (derived property)

    public static class ChangingPermissionDomainEvent extends PropertyDomainEvent<ApplicationPermission> {}

    private ApplicationFeatureId changingFeatureId;
    private ApplicationPermissionMode changingMode;
    private ApplicationPermissionRule changingRule;


    @Property(
            domainEvent = ChangingPermissionDomainEvent.class,
            editing = Editing.DISABLED
            )
    @PropertyLayout(hidden=Where.REFERENCES_PARENT)
    @MemberOrder(name="Cause", sequence = "2.2")
    public ApplicationPermission getChangingPermission() {
        if(getChangingPermissionValue() == null) {
            return null;
        }
        return applicationPermissionRepository.findByUserAndPermissionValue(username, getChangingPermissionValue())
                .orElse(null);
    }

    private ApplicationPermissionValue getChangingPermissionValue() {
        if(changingFeatureId == null) {
            return null;
        }
        return new ApplicationPermissionValue(changingFeatureId, changingRule, changingMode);
    }


    // -- toString

    private static final ToString<UserPermissionViewModel> toString = 
            ObjectContracts
            .toString("user", UserPermissionViewModel::getUser)
            .thenToString("featureId", UserPermissionViewModel::getFeatureId);

    @Override
    public String toString() {
        return toString.toString(this);
    }

    // -- Functions

    public static final class Functions {

        private Functions(){}

        public static Function<ApplicationFeature, UserPermissionViewModel> asViewModel(
                final ApplicationUser user, 
                final FactoryService factoryService) {

            return (final ApplicationFeature input) -> {
                val permissionSet = user.getPermissionSet();
                val changingEvaluation = permissionSet.evaluate(input.getFeatureId(), ApplicationPermissionMode.CHANGING);
                val viewingEvaluation = permissionSet.evaluate(input.getFeatureId(), ApplicationPermissionMode.VIEWING);
                return UserPermissionViewModel
                        .newViewModel(
                                input.getFeatureId(), 
                                user, 
                                viewingEvaluation, 
                                changingEvaluation, 
                                factoryService);
            };
        }
    }


}
