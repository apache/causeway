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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.appfeat.ApplicationFeature;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.collections._Lists;

/**
 * @since 2.x  {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named(ApplicationFeatureMenu.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ApplicationFeatureMenu {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleApplib.NAMESPACE_FEAT + ".ApplicationFeatureMenu";

    public static abstract class PropertyDomainEvent<T>
    extends CausewayModuleApplib.PropertyDomainEvent<ApplicationFeatureMenu, T> {}

    public static abstract class CollectionDomainEvent<T>
    extends CausewayModuleApplib.CollectionDomainEvent<ApplicationFeatureMenu, T> {}

    public static abstract class ActionDomainEvent
    extends CausewayModuleApplib.ActionDomainEvent<ApplicationFeatureMenu> {}


    // -- ICON NAME

    @ObjectSupport public String iconName() {
        return "applicationFeature";
    }


    // -- ALL PACKAGES

    public static class AllNamespacesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllNamespacesDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-list", sequence = "100.40.1")
    public List<ApplicationNamespace> allNamespaces() {
        return asViewModels(featureRepository.allNamespaces(), ApplicationNamespace.class);
    }


    // -- ALL TYPES

    public static class AllTypesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllTypesDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-list", sequence = "100.40.2")
    public List<ApplicationType> allTypes() {
        return asViewModels(featureRepository.allTypes(), ApplicationType.class);
    }


    // -- ALL ACTIONS

    public static class AllActionsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllActionsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-list", sequence = "100.40.3")
    public List<ApplicationTypeAction> allActions() {
        return asViewModels(featureRepository.allActions(), ApplicationTypeAction.class);
    }


    // -- ALL PROPERTIES

    public static class AllPropertiesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllPropertiesDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-list", sequence = "100.40.4")
    public List<ApplicationTypeProperty> allProperties() {
        return asViewModels(featureRepository.allProperties(), ApplicationTypeProperty.class);
    }


    // -- ALL COLLECTIONS

    public static class AllCollectionsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllCollectionsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-list", sequence = "100.40.5")
    public List<ApplicationTypeCollection> allCollections() {
        return asViewModels(featureRepository.allCollections(), ApplicationTypeCollection.class);
    }

    // -- HELPERS

    private <T extends ApplicationFeatureViewModel> List<T> asViewModels(
            final Collection<ApplicationFeature> features,
            final Class<T> viewmodelType) {

        return features.stream()
                .map(ApplicationFeature::getFeatureId)
                .map(ApplicationFeatureViewModel.factory(featureRepository, factory, viewmodelType))
                .collect(_Lists.toUnmodifiable());
    }

    // -- DEPENDENCIES

    @Inject ApplicationFeatureRepository featureRepository;
    @Inject RepositoryService repository;
    @Inject FactoryService factory;


}
