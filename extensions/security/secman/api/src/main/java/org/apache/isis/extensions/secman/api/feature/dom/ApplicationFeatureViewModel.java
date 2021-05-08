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
package org.apache.isis.extensions.secman.api.feature.dom;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Navigable;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRepository;

import lombok.NonNull;
import lombok.val;

/**
 * View model identified by {@link ApplicationFeatureId} and backed by an {@link ApplicationFeature}.
 */
@DomainObject(
        objectType = "isis.ext.secman.ApplicationFeatureViewModel"
        )
public abstract class ApplicationFeatureViewModel implements ViewModel {

    public static abstract class PropertyDomainEvent<S extends ApplicationFeatureViewModel,T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<S, T> {}
    public static abstract class CollectionDomainEvent<S extends ApplicationFeatureViewModel,T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<S, T> {}
    public static abstract class ActionDomainEvent<S extends ApplicationFeatureViewModel> extends IsisModuleExtSecmanApi.ActionDomainEvent<S> {}

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository featureRepository;
    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;

    // -- constructors
    public static ApplicationFeatureViewModel newViewModel(
            final ApplicationFeatureId featureId,
            final ApplicationFeatureRepository applicationFeatureRepository,
            final FactoryService factoryService) {
        final Class<? extends ApplicationFeatureViewModel> cls = viewModelClassFor(featureId, applicationFeatureRepository);
        return factoryService.viewModel(cls, featureId.asEncodedString());
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

            val memberSort =
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
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        return getFullyQualifiedName();
    }
    public String iconName() {
        return "applicationFeature";
    }

    // -- ViewModel impl
    @Override
    public String viewModelMemento() {
        return getFeatureId().asEncodedString();
    }

    @Override
    public void viewModelInit(final String encodedMemento) {
        final ApplicationFeatureId applicationFeatureId = ApplicationFeatureId.parseEncoded(encodedMemento);
        setFeatureId(applicationFeatureId);
    }


    // -- featureId (property, programmatic)
    private ApplicationFeatureId featureId;

    @Programmatic
    public ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    public void setFeatureId(final ApplicationFeatureId applicationFeatureId) {
        this.featureId = applicationFeatureId;
    }


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

    // -- packageName
    public static class NamespaceNameDomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}

    @Property(
            domainEvent = NamespaceNameDomainEvent.class
            )
    @PropertyLayout(
            typicalLength=ApplicationFeatureConstants.TYPICAL_LENGTH_NAMESPACE,
            fieldSetId="Id",
            sequence = "2.2")
    public String getNamespaceName() {
        return getFeatureId().getNamespace();
    }

    // -- className

    public static class TypeSimpleNameDomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}

    /**
     * For packages, will be null. Is in this class (rather than subclasses) so is shown in
     * {@link ApplicationNamespace#getContents() package contents}.
     */
    @Property(
            domainEvent = TypeSimpleNameDomainEvent.class
            )
    @PropertyLayout(
            typicalLength=ApplicationFeatureConstants.TYPICAL_LENGTH_TYPE_SIMPLE_NAME,
            fieldSetId="Id",
            sequence = "2.3")
    public String getTypeSimpleName() {
        return getFeatureId().getTypeSimpleName();
    }
    public boolean hideTypeSimpleName() {
        return getSort().isNamespace();
    }

    // -- memberName

    public static class MemberNameDomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, String> {}

    /**
     * For packages and class names, will be null.
     */
    @Property(
            domainEvent = MemberNameDomainEvent.class
            )
    @PropertyLayout(
            typicalLength=ApplicationFeatureConstants.TYPICAL_LENGTH_MEMBER_NAME,
            fieldSetId="Id",
            sequence = "2.4")
    public String getMemberName() {
        return getFeatureId().getMemberName();
    }

    public boolean hideMemberName() {
        return !getSort().isMember();
    }


    // -- parent (property)

    public static class ParentDomainEvent extends PropertyDomainEvent<ApplicationFeatureViewModel, ApplicationFeatureViewModel> {}

    @Property(
            domainEvent = ParentDomainEvent.class
            )
    @PropertyLayout(
            navigable = Navigable.PARENT,
            hidden = Where.ALL_TABLES,
            fieldSetId = "Parent",
            sequence = "2.6")
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
        return factory.viewModel(cls, parentId.asEncodedString());
    }


    // -- permissions (collection)
    public static class PermissionsDomainEvent extends CollectionDomainEvent<ApplicationFeatureViewModel, ApplicationPermission> {}

    @Collection(
            domainEvent = PermissionsDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sequence = "10"
            )
    public java.util.Collection<? extends ApplicationPermission> getPermissions() {
        return applicationPermissionRepository.findByFeatureCached(getFeatureId());
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
            final @NonNull Class<T> viewmodelType) {

        return featureId -> _Casts.<T>uncheckedCast(ApplicationFeatureViewModel
                .newViewModel(featureId, featureRepository, factory));
    }

    // -- HELPER

    protected <T extends ApplicationFeatureViewModel> List<T> asViewModels(
            final java.util.Collection<ApplicationFeatureId> featureIds,
            final Class<T> viewmodelType) {
        return featureIds.stream()
                .map(factory(featureRepository, factory, viewmodelType))
                .collect(_Lists.toUnmodifiable());
    }

    protected <T extends ApplicationFeatureViewModel> T asViewModel(
            final ApplicationFeatureId featureId,
            final Class<T> viewmodelType) {
        return factory(featureRepository, factory, viewmodelType).apply(featureId);
    }

}
