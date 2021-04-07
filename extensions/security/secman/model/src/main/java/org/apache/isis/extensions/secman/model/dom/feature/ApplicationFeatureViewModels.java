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
package org.apache.isis.extensions.secman.model.dom.feature;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isis.ext.secman.ApplicationFeatureViewModels"
        )
@DomainServiceLayout(
        named="Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class ApplicationFeatureViewModels  {

    public static abstract class PropertyDomainEvent<T> 
    extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationFeatureViewModels, T> {}

    public static abstract class CollectionDomainEvent<T> 
    extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationFeatureViewModels, T> {}

    public static abstract class ActionDomainEvent 
    extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationFeatureViewModels> {}

    // -- ICON NAME

    public String iconName() {
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

    // -- ALL CLASSES

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
            final Class<T> cls) {

        return _Lists.map(
                features,
                ApplicationFeatureViewModel.Functions
                .<T>asViewModel(featureRepository, factory)
                );
    }

    // -- DEPENDENCIES

    @Inject ApplicationFeatureRepository featureRepository;
    @Inject RepositoryService repository;
    @Inject FactoryService factory;


}
