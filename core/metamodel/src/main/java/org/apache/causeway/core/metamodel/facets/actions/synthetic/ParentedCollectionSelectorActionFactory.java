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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetDefault;
import org.apache.causeway.core.metamodel.facets.param.parameter.mandatory.MandatoryFacetForParameterAnnotation;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionDefault;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ParentedCollectionSelectorActionFactory {

    public static final String ACTION_ID_PREFIX = "__causeway_select_";

    public static Stream<ObjectAction> createFor(
            final @NonNull ObjectSpecification parentSpec,
            final @NonNull Stream<ObjectAssociation> associations) {
        return associations
                .filter(ObjectAssociation::isOneToManyAssociation)
                .map(ObjectAssociation::getSpecialization)
                .flatMap(specialization -> specialization.right().stream())
                .filter(collection -> isEligible(parentSpec, collection))
                .map(collection -> create(parentSpec, collection));
    }

    private static boolean isEligible(
            final ObjectSpecification parentSpec,
            final OneToManyAssociation collection) {
        if(collection.getElementType() == null) {
            return false;
        }
        if(parentSpec.isEntity() && collection.getElementType().isEntityOrViewModelOrAbstract()) {
            return true;
        }
        return MetaModelContext.instanceElseFail().getSystemEnvironment().isUnitTesting()
                && parentSpec.isViewModel()
                && collection.getElementType().isViewModel();
    }

    private static ObjectAction create(
            final ObjectSpecification parentSpec,
            final OneToManyAssociation collection) {

        val childSpec = collection.getElementType();
        val scalarProperties = scalarPropertiesOf(childSpec);
        val parameterTypes = parameterTypes(parentSpec, scalarProperties);
        val parameterNames = parameterNames(parentSpec, scalarProperties);
        val actionId = actionIdFor(collection);

        val facetedMethod = FacetedMethod.createSyntheticAction(
                MetaModelContext.instanceElseFail(),
                parentSpec.getCorrespondingClass(),
                actionId,
                childSpec.getCorrespondingClass(),
                parameterTypes,
                parameterNames);

        installActionFacets(parentSpec, collection, scalarProperties, facetedMethod);
        installParameterFacets(scalarProperties, facetedMethod);

        return ObjectActionDefault.forMethod(facetedMethod);
    }

    private static Can<ObjectAssociation> scalarPropertiesOf(final ObjectSpecification childSpec) {
        return childSpec.streamAssociations(MixedIn.INCLUDED)
                .filter(ObjectAssociation::isOneToOneAssociation)
                .filter(property -> property.getElementType() != null && property.getElementType().isValue())
                .collect(Can.toCan());
    }

    private static Class<?>[] parameterTypes(
            final ObjectSpecification parentSpec,
            final Can<ObjectAssociation> scalarProperties) {
        val parameterTypes = new Class<?>[scalarProperties.size() + 1];
        parameterTypes[0] = parentSpec.getCorrespondingClass();
        for(int i = 0; i < scalarProperties.size(); i++) {
            parameterTypes[i + 1] = scalarProperties.getElseFail(i).getElementType().getCorrespondingClass();
        }
        return parameterTypes;
    }

    private static String[] parameterNames(
            final ObjectSpecification parentSpec,
            final Can<ObjectAssociation> scalarProperties) {
        val parameterNames = new String[scalarProperties.size() + 1];
        parameterNames[0] = _Strings.asCamelCaseDecapitalized.apply(parentSpec.getShortIdentifier());
        for(int i = 0; i < scalarProperties.size(); i++) {
            parameterNames[i + 1] = scalarProperties.getElseFail(i).getId();
        }
        return parameterNames;
    }

    private static String actionIdFor(final OneToManyAssociation collection) {
        return ACTION_ID_PREFIX + collection.getId();
    }

    private static void installActionFacets(
            final ObjectSpecification parentSpec,
            final OneToManyAssociation collection,
            final Can<ObjectAssociation> scalarProperties,
            final FacetedMethod facetedMethod) {
        FacetUtil.addFacet(new MemberNamedFacetForStaticMemberName("Select", facetedMethod));
        FacetUtil.addFacet(new LayoutGroupFacetForParentedCollectionSelector(
                collection.getId(), collection.getCanonicalFriendlyName(), facetedMethod));
        FacetUtil.addFacet(new ParentedCollectionSelectorFacetDefault(collection, facetedMethod));
        FacetUtil.addFacet(new ActionSemanticsFacetForParentedCollectionSelector(facetedMethod));
        FacetUtil.addFacet(new ActionValidationFacetForParentedCollectionSelector(
                collection,
                scalarProperties,
                facetedMethod));
        FacetUtil.addFacet(new ActionInvocationFacetForParentedCollectionSelector(
                facetedMethod.getMethod(),
                parentSpec,
                collection.getElementType(),
                collection,
                scalarProperties,
                facetedMethod));
        FacetUtil.addFacetIfPresent(CommandPublishingFacetForActionAnnotation.create(
                Optional.empty(),
                MetaModelContext.instanceElseFail().getConfiguration(),
                MetaModelContext.instanceElseFail().getServiceInjector(),
                facetedMethod).map(CommandPublishingFacet.class::cast));
    }

    private static void installParameterFacets(
            final Can<ObjectAssociation> scalarProperties,
            final FacetedMethod facetedMethod) {
        val parameters = facetedMethod.getParameters();
        FacetUtil.addFacet(new ParamNamedFacetForParentedCollectionSelector(
                facetedMethod.getMethod().getParameterName(0), parameters.getElseFail(0)));
        FacetUtil.addFacet(MandatoryFacetDefault.required(parameters.getElseFail(0)));
        FacetUtil.addFacet(new ActionParameterChoicesFacetForParentedCollectionSelectorParent(parameters.getElseFail(0)));
        FacetUtil.addFacet(new ActionParameterDefaultsFacetForParentedCollectionSelectorParent(parameters.getElseFail(0)));
        FacetUtil.addFacet(new DisabledFacetForParentedCollectionSelectorParent(parameters.getElseFail(0)));
        for(int i = 0; i < scalarProperties.size(); i++) {
            val parameter = parameters.getElseFail(i + 1);
            FacetUtil.addFacet(new ParamNamedFacetForParentedCollectionSelector(
                    scalarProperties.getElseFail(i).getCanonicalFriendlyName(), parameter));
            FacetUtil.addFacet(new MandatoryFacetForParameterAnnotation.Optional(parameter));
        }
    }

}
