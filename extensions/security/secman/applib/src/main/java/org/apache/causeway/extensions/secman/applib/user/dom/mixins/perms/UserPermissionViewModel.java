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
package org.apache.causeway.extensions.secman.applib.user.dom.mixins.perms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.services.appfeatui.ApplicationFeatureViewModel;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.resources._Serializables;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * View model identified by {@link ApplicationFeatureId} and backed by an
 * {@link ApplicationFeature}.
 *
 * @since 2.0 {@index}
 */
@Named(UserPermissionViewModel.LOGICAL_TYPE_NAME)
@DomainObject(
        nature = Nature.VIEW_MODEL)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class UserPermissionViewModel implements ViewModel {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".UserPermissionViewModel";

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSecmanApplib.PropertyDomainEvent<UserPermissionViewModel, T> {}


    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject FactoryService factory;
    @Inject ApplicationFeatureRepository featureRepository;
    @Inject ApplicationPermissionRepository applicationPermissionRepository;

    // -- constructors, factory methods

    public static String memento(
            final ApplicationFeatureId featureId,
            final ApplicationUser user,
            final ApplicationPermissionValueSet.Evaluation viewingEvaluation,
            final ApplicationPermissionValueSet.Evaluation changingEvaluation) {
        return asEncodedString(featureId, user.getUsername(), viewingEvaluation, changingEvaluation);
    }

    public UserPermissionViewModel() {
        setFeatureId(ApplicationFeatureId.NAMESPACE_DEFAULT);
    }


    // -- identification

    @ObjectSupport public String title() {
        return getVerb() + " " + getFeatureId().getFullyQualifiedName();
    }

    @ObjectSupport public String iconName() {
        return "userPermission";
    }

    // -- VIEWMODEL CONTRACT

    @Inject
    public UserPermissionViewModel(final String memento) {
        val payload = _Serializables.read(String[].class,
                _Bytes.ofUrlBase64.apply(memento.getBytes(StandardCharsets.US_ASCII)));
        parse(payload);
    }

    @Override
    public String viewModelMemento() {
        return asEncodedString(getFeatureId(), getUsername(),
                newEvaluation(viewingGranted, viewingFeatureId, viewingRule, viewingMode),
                newEvaluation(changingGranted, changingFeatureId, changingRule, changingMode));
    }

    private static String asEncodedString(
            final ApplicationFeatureId featureId,
            final String username,
            final ApplicationPermissionValueSet.Evaluation viewingEvaluation,
            final ApplicationPermissionValueSet.Evaluation changingEvaluation) {

        final ApplicationPermissionValue viewingEvaluationCause = viewingEvaluation.getCause();
        final ApplicationFeatureId viewingEvaluationCauseFeatureId = viewingEvaluationCause != null
                ? viewingEvaluationCause.getFeatureId()
                : null;

        final ApplicationPermissionValue changingEvaluationCause = changingEvaluation.getCause();
        final ApplicationFeatureId changingEvaluationCauseFeatureId = changingEvaluationCause != null
                ? changingEvaluationCause.getFeatureId()
                : null;

        val payload = new String[] {
            username,

            ""+viewingEvaluation.isGranted(),
            viewingEvaluationCauseFeatureId != null? ""+viewingEvaluationCauseFeatureId.getSort(): "",
            viewingEvaluationCauseFeatureId != null? ""+viewingEvaluationCauseFeatureId.getFullyQualifiedName(): "",
            viewingEvaluationCause != null? ""+viewingEvaluationCause.getRule(): "",
            viewingEvaluationCause != null? ""+viewingEvaluationCause.getMode(): "",

            ""+changingEvaluation.isGranted(),
            changingEvaluationCauseFeatureId != null? ""+changingEvaluationCauseFeatureId.getSort(): "",
            changingEvaluationCauseFeatureId != null? ""+changingEvaluationCauseFeatureId.getFullyQualifiedName(): "",
            changingEvaluationCause != null? ""+changingEvaluationCause.getRule(): "",
            changingEvaluationCause != null? ""+changingEvaluationCause.getMode(): "",

            ""+featureId.getSort(),
            featureId.getFullyQualifiedName()
        };
        return new String(_Bytes.asUrlBase64.apply(_Serializables.write(payload)), StandardCharsets.US_ASCII);
    }

    private void parse(final String[] payload) {
        final Iterator<String> iterator = Arrays.asList(payload).iterator();

        this.username = iterator.next();

        this.viewingGranted = Boolean.parseBoolean(iterator.next());
        final String viewingEvaluationCauseFeatureIdType = iterator.next();

        final ApplicationFeatureSort viewingEvaluationFeatureIdType = !viewingEvaluationCauseFeatureIdType.isEmpty()
                ? ApplicationFeatureSort.valueOf(viewingEvaluationCauseFeatureIdType)
                : null;
        final String viewingEvaluationFeatureFqn = iterator.next();
        this.viewingFeatureId = viewingEvaluationFeatureIdType != null
                ? ApplicationFeatureId.newFeature(viewingEvaluationFeatureIdType, viewingEvaluationFeatureFqn)
                : null;

        final String viewingEvaluationCauseRule = iterator.next();
        this.viewingRule = !viewingEvaluationCauseRule.isEmpty()? ApplicationPermissionRule.valueOf(viewingEvaluationCauseRule): null;
        final String viewingEvaluationCauseMode = iterator.next();
        this.viewingMode = !viewingEvaluationCauseMode.isEmpty()? ApplicationPermissionMode.valueOf(viewingEvaluationCauseMode): null;


        this.changingGranted = Boolean.parseBoolean(iterator.next());
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

    // -- user (derived property, hidden in parented tables)

    @Property(
            domainEvent = User.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "identity",
            hidden=Where.PARENTED_TABLES,
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface User {
        class DomainEvent extends PropertyDomainEvent<ApplicationUser> {}
    }

    @User
    public ApplicationUser getUser() {
        return applicationUserRepository.findOrCreateUserByUsername(getUsername());
    }

    @Getter(onMethod_ = {@Programmatic})
    private String username;

    // -- verb (derived property)

    private boolean viewingGranted;
    private boolean changingGranted;

    @Property (
            domainEvent = Verb.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "2",
            typicalLength= Verb.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Verb {
        int TYPICAL_LENGTH = 12;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Verb
    public String getVerb() {
        return changingGranted
                ? "Can change"
                        : viewingGranted
                        ? "Can view"
                                : "No access to";
    }

    // -- feature (derived property)

    @Property(
            domainEvent = Feature.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "identity",
            hidden=Where.REFERENCES_PARENT,
            sequence = "4"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Feature {
        class DomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel> {}
    }

    @Feature
    public ApplicationFeatureViewModel getFeature() {
        if(getFeatureId() == null) {
            return null;
        }
        return ApplicationFeatureViewModel.newViewModel(getFeatureId(), featureRepository, factory);
    }

    @Getter(onMethod_ = {@Programmatic})
    @Setter
    private ApplicationFeatureId featureId;

    // -- viewingPermission (derived property)

    private ApplicationFeatureId viewingFeatureId;
    private ApplicationPermissionMode viewingMode;
    private ApplicationPermissionRule viewingRule;

    @Property(
            domainEvent = ViewingPermission.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "cause",
            hidden = Where.REFERENCES_PARENT,
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface ViewingPermission {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermission> {}
    }

    @ViewingPermission
    public ApplicationPermission getViewingPermission() {
        return getViewingPermissionValue() == null
                ? null
                : applicationPermissionRepository.findByUserAndPermissionValue(username, getViewingPermissionValue())
                  .orElse(null);
    }

    private ApplicationPermissionValue getViewingPermissionValue() {
        return viewingFeatureId == null
                ? null
                : new ApplicationPermissionValue(viewingFeatureId, viewingRule, viewingMode);
    }

    // -- changingPermission (derived property)

    private ApplicationFeatureId changingFeatureId;
    private ApplicationPermissionMode changingMode;
    private ApplicationPermissionRule changingRule;

    @Property(
            domainEvent = ChangingPermission.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "cause",
            hidden = Where.REFERENCES_PARENT,
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface ChangingPermission {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermission> {}
    }

    @ChangingPermission
    public ApplicationPermission getChangingPermission() {
        return getChangingPermissionValue() == null
                ? null
                : applicationPermissionRepository.findByUserAndPermissionValue(username, getChangingPermissionValue())
                    .orElse(null);
    }

    private ApplicationPermissionValue getChangingPermissionValue() {
        return changingFeatureId == null
                ? null
                : new ApplicationPermissionValue(changingFeatureId, changingRule, changingMode);
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

    // -- Factory

    public static Function<ApplicationFeatureId, UserPermissionViewModel> asViewModel(
            final ApplicationUser user,
            final FactoryService factoryService) {

        return (final ApplicationFeatureId featureId) -> {
            val permissionSet = user.getPermissionSet();
            val changingEvaluation = permissionSet.evaluate(featureId, ApplicationPermissionMode.CHANGING);
            val viewingEvaluation = permissionSet.evaluate(featureId, ApplicationPermissionMode.VIEWING);
            return factoryService.viewModel(new UserPermissionViewModel(memento(
                    featureId,
                    user,
                    viewingEvaluation,
                    changingEvaluation)));
        };
    }

}
