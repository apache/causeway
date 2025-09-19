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
package org.apache.causeway.applib.services.appfeatui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.util.Equality;
import org.apache.causeway.applib.util.Hashing;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.Setter;

/**
 * View model identified by {@link ApplicationFeatureId} and backed by an {@link ApplicationFeature}.
 *
 * @since 2.x  {@index}
 */
@Named(ApplicationFeatureViewModel.LOGICAL_TYPE_NAME)
@DomainObject
public abstract class ApplicationFeatureViewModel implements ViewModel {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE_FEAT + ".ApplicationFeatureViewModel";

    public static abstract class PropertyDomainEvent<S extends ApplicationFeatureViewModel,T> extends CausewayModuleApplib.PropertyDomainEvent<S, T> {}
    public static abstract class CollectionDomainEvent<S extends ApplicationFeatureViewModel,T> extends CausewayModuleApplib.CollectionDomainEvent<S, T> {}
    public static abstract class ActionDomainEvent<S extends ApplicationFeatureViewModel> extends CausewayModuleApplib.ActionDomainEvent<S> {}

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository featureRepository;

    // -- constructors

    public static ApplicationFeatureViewModel newViewModel(
            final ApplicationFeatureId featureId,
            final ApplicationFeatureRepository applicationFeatureRepository,
            final FactoryService factoryService) {
        final Class<? extends ApplicationFeatureViewModel> vmClass =
                viewModelClassFor(featureId, applicationFeatureRepository);

        if(featureId.getSort().isNamespace()) {
            _Assert.assertEquals(vmClass, ApplicationNamespace.class);
            return factoryService.viewModel(new ApplicationNamespace(featureId));
        }

        return factoryService.viewModel(vmClass,
                Bookmark.forLogicalTypeNameAndIdentifier(
                        featureId.getLogicalTypeName(),
                        featureId.asEncodedString()));
    }

    private static Class<? extends ApplicationFeatureViewModel> viewModelClassFor(
            final ApplicationFeatureId featureId,
            final ApplicationFeatureRepository applicationFeatureRepository) {
        switch (featureId.getSort()) {
        case NAMESPACE:
            return ApplicationNamespace.class;
        case TYPE:
            return ApplicationType.class;
        case MEMBER:

            var memberSort =
            Optional.ofNullable(applicationFeatureRepository.findFeature(featureId))
                .flatMap(ApplicationFeature::getMemberSort)
                .orElse(null);

            if(memberSort != null) {
                switch(memberSort) {
                case PROPERTY:
                    return ApplicationTypeProperty.class;
                case COLLECTION:
                    return ApplicationTypeCollection.class;
                case ACTION:
                    return ApplicationTypeAction.class;
                }
            }

        }
        throw new IllegalArgumentException("could not determine feature type; featureId = " + featureId);
    }

    public ApplicationFeatureViewModel() {
        this(ApplicationFeatureId.NAMESPACE_DEFAULT);
    }

    ApplicationFeatureViewModel(final ApplicationFeatureId featureId) {
        setFeatureId(featureId);
    }

    // -- identification
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Causeway 1.6.0
     */
    @ObjectSupport public String title() {
        return getFullyQualifiedName();
    }
    @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconSize iconSize) {
        return new ObjectSupport.ClassPathIconResource("applicationFeature");
    }

    // -- VIEWMODEL CONTRACT

    public ApplicationFeatureViewModel(final String encodedMemento) {
        this(ApplicationFeatureId.parseEncoded(encodedMemento));
    }

    @Override
    public String viewModelMemento() {
        return getFeatureId().asEncodedString();
    }

    // -- featureId (property, programmatic)

    @Getter(onMethod_ = {@Programmatic})
    @Setter
    private ApplicationFeatureId featureId;

    // -- feature (property, programmatic)

    @Programmatic
    ApplicationFeature getFeature() {
        return featureRepository.findFeature(getFeatureId());
    }

    // -- fullyQualifiedName (property, programmatic)
    @Programmatic // in the title
    public String getFullyQualifiedName() {
        return getFeatureId().getFullyQualifiedName();
    }

    // -- type (programmatic)
    @Programmatic
    public ApplicationFeatureSort getSort() {
        return getFeatureId().getSort();
    }

    @Property(
            domainEvent = NamespaceName.DomainEvent.class,
            maxLength = NamespaceName.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "2.2",
            typicalLength = NamespaceName.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = NamespaceName.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Namespace Name",
            typicalLength = NamespaceName.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NamespaceName {
        int MAX_LENGTH = 50;
        int TYPICAL_LENGTH = 50;

        class DomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}
    }

    @NamespaceName
    public String getNamespaceName() {
        return getFeatureId().getNamespace();
    }

    // -- className

    /**
     * For packages, will be null. Is in this class (rather than subclasses) so is shown in
     * {@link ApplicationNamespace#getContents() package contents}.
     */

    @Property(
            domainEvent = TypeSimpleName.DomainEvent.class,
            maxLength = TypeSimpleName.MAX_LENGTH
    )
    @PropertyLayout(
            typicalLength = TypeSimpleName.TYPICAL_LENGTH,
            fieldSetId = "identity",
            sequence = "2.3"
    )
    @Parameter(
            maxLength = TypeSimpleName.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Type Simple Name",
            typicalLength = TypeSimpleName.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TypeSimpleName {

        int MAX_LENGTH = 50;
        int TYPICAL_LENGTH = 50;

        class DomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}
    }

    @TypeSimpleName
    public String getTypeSimpleName() {
        return getFeatureId().getTypeSimpleName();
    }
    @MemberSupport public boolean hideTypeSimpleName() {
        return getSort().isNamespace();
    }

    // -- memberName

    @Property(
            domainEvent = MemberName.DomainEvent.class,
            maxLength = MemberName.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "2.4",
            typicalLength = MemberName.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = MemberName.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Member Name",
            typicalLength = MemberName.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MemberName {

        int MAX_LENGTH = 50;
        int TYPICAL_LENGTH = 50;

        class DomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}
    }

    /**
     * For packages and class names, will be null.
     */
    @MemberName
    public String getMemberName() {
        return getFeatureId().getLogicalMemberName();
    }
    @MemberSupport public boolean hideMemberName() {
        return !getSort().isMember();
    }

    // -- parent (property)

    @Property(
            domainEvent = Parent.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "parent",
            hidden = Where.ALL_TABLES,
            navigable = Navigable.PARENT,
            sequence = "2.6"
    )
    @ParameterLayout(
            named = "Parent"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parent {
        class DomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, ApplicationFeatureViewModel> {}
    }

    @Parent
    public ApplicationFeatureViewModel getParent() {
        final ApplicationFeatureId parentId;
        parentId = getSort() == ApplicationFeatureSort.MEMBER
                ? getFeatureId().getParentTypeFeatureId()
                : getFeatureId().getParentNamespaceFeatureId();
        if(parentId == null) {
            return null;
        }
        final ApplicationFeature feature = featureRepository.findFeature(parentId);
        if (feature == null) {
            return null;
        }
        final Class<? extends ApplicationFeatureViewModel> cls =
                viewModelClassFor(parentId, featureRepository);
        return factory.viewModel(cls,
                Bookmark.forLogicalTypeNameAndIdentifier(
                        ApplicationFeatureViewModel.LOGICAL_TYPE_NAME,
                        parentId.asEncodedString()));
    }

    // -- parentPackage (property, programmatic, for packages & classes only)

    /**
     * The parent package feature of this class or package.
     */
    @Programmatic
    public ApplicationFeatureViewModel getParentNamespace() {
        return ApplicationFeatureViewModel
        .newViewModel(getFeatureId().getParentNamespaceFeatureId(), featureRepository, factory);
    }

    // -- equals, hashCode, toString

    private static final Equality<ApplicationFeatureViewModel> equality =
            ObjectContracts.checkEquals(ApplicationFeatureViewModel::getFeatureId);

    private static final Hashing<ApplicationFeatureViewModel> hashing =
            ObjectContracts.hashing(ApplicationFeatureViewModel::getFeatureId);

    private static final ToString<ApplicationFeatureViewModel> toString =
            ObjectContracts.toString("featureId", ApplicationFeatureViewModel::getFeatureId);

    @Override
    public boolean equals(final Object obj) {
        return equality.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }

    // -- FACTORY

    public static <T extends ApplicationFeatureViewModel> Function<ApplicationFeatureId, T> factory(
            final @NonNull ApplicationFeatureRepository featureRepository,
            final @NonNull FactoryService factory,
            final @NonNull Class<T> viewModelType) {

        return featureId -> _Casts.<T>uncheckedCast(ApplicationFeatureViewModel
                .newViewModel(featureId, featureRepository, factory));
    }

    // -- HELPER

    protected <T extends ApplicationFeatureViewModel> List<T> asViewModels(
            final java.util.Collection<ApplicationFeatureId> featureIds,
            final Class<T> viewModelType) {
        return featureIds.stream()
                .map(factory(featureRepository, factory, viewModelType))
                .collect(_Lists.toUnmodifiable());
    }

    protected <T extends ApplicationFeatureViewModel> T asViewModel(
            final ApplicationFeatureId featureId,
            final Class<T> viewModelType) {
        return factory(featureRepository, factory, viewModelType).apply(featureId);
    }

}
