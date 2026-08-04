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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.metamodel.MetaModelService.AssociationsLookup;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForScalarReferenceNavigation;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionValidationFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.CssClassFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForEmptyParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForNullScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.FaFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutGroupFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutGroupFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutOrderFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParamNamedFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParentedCollectionNavigationFacetDefault;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacetDefault;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer.ColumnQuery;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

final class SyntheticNavigationActionFactory {

    static final String ACTION_ID_PREFIX = "__causeway_navigate_to_";

    private static final Set<String> EXCLUDED_PARAMETER_PROPERTY_IDS = Set.of(
            "logicalTypeName",
            "id",
            "version",
            "objectIdentifier",
            "datanucleusVersionLong",
            "datanucleusVersionTimestamp");

    private SyntheticNavigationActionFactory() {
    }

    static Stream<ObjectAction> createFor(
            final MetaModelContext mmc,
            final ObjectSpecification ownerSpec,
            final Stream<ObjectAssociation> associations,
            final Set<String> existingActionIds,
            final Set<String> existingSyntheticActionIds) {

        if (!(ownerSpec.isEntity() || ownerSpec.isViewModel())
                || CommandRecordingSuppressed.class.isAssignableFrom(ownerSpec.getCorrespondingClass())) {
            return Stream.empty();
        }

        var candidates = associations.toList();
        var generatedIds = new HashSet<String>();

        return Stream.concat(
                        candidates.stream()
                                .filter(ObjectAssociation::isOneToManyAssociation)
                                .map(ObjectAssociation::getSpecialization)
                                .flatMap(specialization -> specialization.right().stream())
                                .filter(collection -> eligible(ownerSpec, collection))
                                .filter(collection -> !existingSyntheticActionIds.contains(
                                        ACTION_ID_PREFIX + collection.getId()))
                                .map(collection -> createCollectionAction(mmc, ownerSpec, collection)),
                        candidates.stream()
                                .filter(ObjectAssociation::isOneToOneAssociation)
                                .map(ObjectAssociation::getSpecialization)
                                .flatMap(specialization -> specialization.left().stream())
                                .filter(reference -> eligible(ownerSpec, reference))
                                .filter(reference -> !existingSyntheticActionIds.contains(
                                        ACTION_ID_PREFIX + reference.getId()))
                                .map(reference -> createReferenceAction(mmc, ownerSpec, reference)))
                .peek(action -> {
                    if (existingActionIds.contains(action.getId()) || !generatedIds.add(action.getId())) {
                        throw new IllegalStateException("Action id '%s' is reserved for synthetic navigation"
                                .formatted(action.getId()));
                    }
                });
    }

    private static boolean eligible(
            final ObjectSpecification ownerSpec,
            final OneToManyAssociation collection) {
        return ownerSpec == collection.getDeclaringType()
                && collection.getElementType() != null
                && collection.getElementType().isEntityOrViewModelOrAbstract();
    }

    private static boolean eligible(
            final ObjectSpecification ownerSpec,
            final OneToOneAssociation reference) {
        return ownerSpec == reference.getDeclaringType()
                && reference.getElementType() != null
                && reference.getElementType().isEntityOrViewModelOrAbstract();
    }

    private static ObjectAction createCollectionAction(
            final MetaModelContext mmc,
            final ObjectSpecification ownerSpec,
            final OneToManyAssociation collection) {

        var filterProperties = filterPropertiesOf(ownerSpec, collection);
        var parameterTypes = filterProperties.stream()
                .map(ObjectAssociation::getElementType)
                .map(ObjectSpecification::getCorrespondingClass)
                .map(ClassUtils::resolvePrimitiveIfNecessary)
                .toArray(Class<?>[]::new);
        var parameterNames = filterProperties.stream()
                .map(ObjectAssociation::getId)
                .toArray(String[]::new);
        var facetedMethod = FacetedMethod.createSyntheticAction(
                mmc,
                ownerSpec.getCorrespondingClass(),
                ACTION_ID_PREFIX + collection.getId(),
                collection.getElementType().getCorrespondingClass(),
                parameterTypes,
                parameterNames);

        installCommonFacets(mmc, facetedMethod);
        FacetUtil.addFacet(new LayoutGroupFacetForParentedCollectionNavigation(
                collection.getId(), collection.getCanonicalFriendlyName(), facetedMethod));
        FacetUtil.addFacet(new LayoutOrderFacetForParentedCollectionNavigation(collection, facetedMethod));
        FacetUtil.addFacet(new ParentedCollectionNavigationFacetDefault(collection, facetedMethod));
        FacetUtil.addFacet(new DisabledFacetForEmptyParentedCollectionNavigation(collection, facetedMethod));
        FacetUtil.addFacet(new ActionValidationFacetForParentedCollectionNavigation(
                collection, filterProperties, facetedMethod));
        FacetUtil.addFacet(new ActionInvocationFacetForParentedCollectionNavigation(
                ownerSpec, collection.getElementType(), collection, filterProperties, facetedMethod));
        installParameterFacets(filterProperties, facetedMethod);

        return ObjectActionDefault.forMethod(facetedMethod);
    }

    private static ObjectAction createReferenceAction(
            final MetaModelContext mmc,
            final ObjectSpecification ownerSpec,
            final OneToOneAssociation reference) {

        var facetedMethod = FacetedMethod.createSyntheticAction(
                mmc,
                ownerSpec.getCorrespondingClass(),
                ACTION_ID_PREFIX + reference.getId(),
                reference.getElementType().getCorrespondingClass(),
                new Class<?>[0],
                new String[0]);

        installCommonFacets(mmc, facetedMethod);
        FacetUtil.addFacet(new LayoutGroupFacetForScalarReferenceNavigation(
                reference.getId(), reference.getCanonicalFriendlyName(), facetedMethod));
        FacetUtil.addFacet(new ScalarReferenceNavigationFacetDefault(reference, facetedMethod));
        FacetUtil.addFacet(new DisabledFacetForNullScalarReferenceNavigation(reference, facetedMethod));
        FacetUtil.addFacet(new ActionInvocationFacetForScalarReferenceNavigation(
                ownerSpec, reference.getElementType(), reference, facetedMethod));

        return ObjectActionDefault.forMethod(facetedMethod);
    }

    private static void installCommonFacets(
            final MetaModelContext mmc,
            final FacetedMethod facetedMethod) {
        FacetUtil.addFacet(new MemberNamedFacetForStaticMemberName("Navigate To", facetedMethod));
        FacetUtil.addFacet(new CssClassFacetForParentedCollectionNavigation(facetedMethod));
        FacetUtil.addFacet(new FaFacetForParentedCollectionNavigation(facetedMethod));
        FacetUtil.addFacet(new ActionSemanticsFacet(
                "synthetic navigation", SemanticsOf.SAFE, facetedMethod));
        FacetUtil.addFacetIfPresent(CommandPublishingFacetForActionAnnotation.create(
                Optional.empty(),
                mmc.getConfiguration(),
                mmc.getServiceInjector(),
                facetedMethod));
    }

    private static Can<ObjectAssociation> filterPropertiesOf(
            final ObjectSpecification ownerSpec,
            final OneToManyAssociation collection) {
        var parentPlaceholder = ManagedObject.empty(ownerSpec);
        var columnQuery = new ColumnQuery(
                collection.getFeatureIdentifier(),
                parentPlaceholder,
                AssociationsLookup.AVAILABLE);
        return collection.getElementType()
                .streamAssociationsForColumnRendering(columnQuery)
                .filter(SyntheticNavigationActionFactory::eligibleFilterProperty)
                .collect(Can.toCan());
    }

    private static boolean eligibleFilterProperty(final ObjectAssociation property) {
        if (!property.isOneToOneAssociation()
                || EXCLUDED_PARAMETER_PROPERTY_IDS.contains(property.getId())
                || property.getElementType() == null) {
            return false;
        }
        var elementType = property.getElementType();
        if (elementType.isValue()) {
            var type = elementType.getCorrespondingClass();
            return type != Blob.class && type != Clob.class;
        }
        return property.containsNonFallbackFacet(PropertyChoicesFacet.class)
                || property.containsNonFallbackFacet(PropertyAutoCompleteFacet.class)
                || elementType.containsNonFallbackFacet(ChoicesFacet.class)
                || elementType.containsNonFallbackFacet(AutoCompleteFacet.class);
    }

    private static void installParameterFacets(
            final Can<ObjectAssociation> filterProperties,
            final FacetedMethod facetedMethod) {
        for (int i = 0; i < filterProperties.size(); i++) {
            var property = filterProperties.getElseFail(i);
            var parameter = facetedMethod.parameters().getElseFail(i);
            FacetUtil.addFacet(new ParamNamedFacetForParentedCollectionNavigation(
                    property.getCanonicalFriendlyName(), parameter));
            FacetUtil.addFacet(new MandatoryFacetForParameterAnnotation.Optional(parameter));
        }
    }
}
