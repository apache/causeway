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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.metamodel.MetaModelService.AssociationsLookup;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionInvocationFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ActionValidationFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.CssClassFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForEmptyParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.DisabledFacetForNullScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.FaFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutGroupFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutGroupFacetForScalarReferenceNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.LayoutOrderFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParamNamedFacetForParentedCollectionNavigation;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParentedCollectionNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParentedCollectionNavigationFacetDefault;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacetDefault;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer.ColumnQuery;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.springframework.util.ClassUtils;

record SyntheticNavigationActionFactory(
		ObjectSpecificationInternal ownerSpec,
		List<ObjectAssociation> regularAssociations,
		List<ObjectAssociation> mixedInAssociations,
		List<ObjectAction> regularActions,
		List<ObjectActionMixedIn> mixedInActions) {

    static final String ACTION_ID_PREFIX = "__causeway_navigate_to_";

    /**
     * Reserved id prefix for a parented-collection selector ("navigate to one of") action. Distinct from the
     * scalar-reference {@link #ACTION_ID_PREFIX} so the two synthetic forms are disambiguated in the serialized
     * command DTO (and so a collection command can be replayed compatibly). Note this value <i>starts with</i>
     * {@link #ACTION_ID_PREFIX}.
     * <p>
     * Kept in sync with {@code CommandExecutorServiceDefault}'s replay-side copy of this prefix.
     */
    static final String COLLECTION_ACTION_ID_PREFIX = ACTION_ID_PREFIX + "one_of_";

    private static final Set<String> EXCLUDED_PARAMETER_PROPERTY_IDS = Set.of(
            "logicalTypeName",
            "id",
            "version",
            "objectIdentifier",
            "datanucleusVersionLong",
            "datanucleusVersionTimestamp");

	List<ObjectAction> synthesizeNavigationActions() {
        var existingActionIds = Stream.concat(regularActions.stream(), mixedInActions.stream())
                .map(ObjectAction::getId)
                .collect(Collectors.toSet());
        var existingSyntheticActionIds = Stream.concat(regularActions.stream(), mixedInActions.stream())
                .filter(action -> action.lookupFacet(ParentedCollectionNavigationFacet.class).isPresent()
                        || action.lookupFacet(ScalarReferenceNavigationFacet.class).isPresent())
                .map(ObjectAction::getId)
                .collect(Collectors.toSet());
        var syntheticActions = createFor(
        				Stream.concat(regularAssociations.stream(), mixedInAssociations.stream()).toList(),
                        existingActionIds,
                        existingSyntheticActionIds)
                .toList();
        return syntheticActions;
    }

    private Stream<ObjectAction> createFor(
            final List<ObjectAssociation> candidates,
            final Set<String> existingActionIds,
            final Set<String> existingSyntheticActionIds) {

    	final Class<?> ownerType = ownerSpec.correspondingClass();

        if (!(ownerSpec.isEntity() || ownerSpec.isViewModel())
                || CommandRecordingSuppressed.class.isAssignableFrom(ownerSpec.correspondingClass()))
			return Stream.empty();

        var generatedIds = new HashSet<String>();

        return Stream.concat(
                        candidates.stream()
                                .filter(ObjectAssociation::isOneToManyAssociation)
                                .map(ObjectAssociation::getSpecialization)
                                .flatMap(specialization -> specialization.right().stream())
                                .filter(collection -> eligible(ownerType, collection))
                                .filter(collection -> !existingSyntheticActionIds.contains(
                                        COLLECTION_ACTION_ID_PREFIX + collection.getId()))
                                .map(collection -> createCollectionAction(ownerSpec, collection)),
                        candidates.stream()
                                .filter(ObjectAssociation::isOneToOneAssociation)
                                .map(ObjectAssociation::getSpecialization)
                                .flatMap(specialization -> specialization.left().stream())
                                .filter(reference -> eligible(ownerType, reference))
                                .filter(reference -> !existingSyntheticActionIds.contains(
                                        ACTION_ID_PREFIX + reference.getId()))
                                .map(reference -> createReferenceAction(ownerSpec, reference)))
                .peek(action -> {
                    if (existingActionIds.contains(action.getId()) || !generatedIds.add(action.getId()))
						throw new IllegalStateException("Action id '%s' is reserved for synthetic navigation"
                                .formatted(action.getId()));
                });
    }

    private static boolean eligible(
            final Class<?> ownerType,
            final OneToManyAssociation collection) {
        return ownerType.equals(collection.getDeclaringType().getClass())
                && collection.getElementType() != null
                && collection.getElementType().isEntityOrViewModelOrAbstract();
    }

    private static boolean eligible(
    		final Class<?> ownerType,
            final OneToOneAssociation reference) {
        return ownerType.equals(reference.getDeclaringType().correspondingClass())
                && reference.getElementType() != null
                && reference.getElementType().isEntityOrViewModelOrAbstract();
    }

    private static ObjectAction createCollectionAction(
    		final ObjectSpecification ownerSpec,
            final OneToManyAssociation collection) {

    	final Class<?> ownerType = ownerSpec.correspondingClass();

        var filterProperties = filterPropertiesOf(ownerType, collection);
        var parameterTypes = filterProperties.stream()
                .map(ObjectAssociation::getElementType)
                .map(ObjectSpecification::correspondingClass)
                .map(ClassUtils::resolvePrimitiveIfNecessary)
                .toArray(Class<?>[]::new);
        var parameterNames = filterProperties.stream()
                .map(ObjectAssociation::getId)
                .toArray(String[]::new);
        var facetedMethod = FacetedMethod.createSyntheticAction(
        		collection.getMetaModelContext(),
        		ownerType,
                COLLECTION_ACTION_ID_PREFIX + collection.getId(),
                collection.getElementType().correspondingClass(),
                parameterTypes,
                parameterNames);

        installCommonFacets(facetedMethod);
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
            final ObjectSpecification ownerSpec,
            final OneToOneAssociation reference) {

        var facetedMethod = FacetedMethod.createSyntheticAction(
        		reference.getMetaModelContext(),
                ownerSpec.correspondingClass(),
                ACTION_ID_PREFIX + reference.getId(),
                reference.getElementType().correspondingClass(),
                new Class<?>[0],
                new String[0]);

        installCommonFacets(facetedMethod);
        FacetUtil.addFacet(new LayoutGroupFacetForScalarReferenceNavigation(
                reference.getId(), reference.getCanonicalFriendlyName(), facetedMethod));
        FacetUtil.addFacet(new ScalarReferenceNavigationFacetDefault(reference, facetedMethod));
        FacetUtil.addFacet(new DisabledFacetForNullScalarReferenceNavigation(reference, facetedMethod));
        FacetUtil.addFacet(new ActionInvocationFacetForScalarReferenceNavigation(
                ownerSpec, reference.getElementType(), reference, facetedMethod));

        return ObjectActionDefault.forMethod(facetedMethod);
    }

    private static void installCommonFacets(
            final FacetedMethod facetedMethod) {
        FacetUtil.addFacet(new MemberNamedFacetForStaticMemberName("Navigate To", facetedMethod));
        FacetUtil.addFacet(new CssClassFacetForParentedCollectionNavigation(facetedMethod));
        FacetUtil.addFacet(new FaFacetForParentedCollectionNavigation(facetedMethod));
        FacetUtil.addFacet(new ActionSemanticsFacet(
                "synthetic navigation", SemanticsOf.SAFE, facetedMethod));
        FacetUtil.addFacetIfPresent(CommandPublishingFacetForActionAnnotation.create(
                Optional.empty(),
                facetedMethod.getConfiguration(),
                facetedMethod.getServiceInjector(),
                facetedMethod));
    }

    private static Can<ObjectAssociation> filterPropertiesOf(
            final Class<?> ownerType,
            final OneToManyAssociation collection) {
        var columnQuery = new ColumnQuery(
                collection.getFeatureIdentifier(),
                ownerType,
                AssociationsLookup.AVAILABLE);
        var elementType = (ObjectSpecificationInternal)collection.getElementType();
        if(!elementType.isFullyIntrospected()) {
        	var specLoaderInternal = (SpecificationLoaderInternal) collection.getSpecificationLoader();
        	specLoaderInternal.loadSpecification(elementType.correspondingClass(), IntrospectionRequest.FULL);
		}
        return collection.getElementType()
                .streamAssociationsForColumnRendering(columnQuery)
                .filter(SyntheticNavigationActionFactory::eligibleFilterProperty)
                .collect(Can.toCan());
    }

    private static boolean eligibleFilterProperty(final ObjectAssociation property) {
        if (!property.isOneToOneAssociation()
                || EXCLUDED_PARAMETER_PROPERTY_IDS.contains(property.getId())
                || property.getElementType() == null)
			return false;
        var elementType = property.getElementType();
        if (elementType.isValue()) {
            var type = elementType.correspondingClass();
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
