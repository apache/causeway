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

import java.util.List;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationFacade.ObjectMetaData;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationFacade.ObjectMetaDataFull;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationFacade.ObjectMetaDataInitial;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationFacade.ObjectMetaDataTypeOnly;

/**
 * WIP
 */
record ObjectMetaDataFactory(
		SpecificationLoaderInternal specLoaderInternal,
		FacetProcessor facetProcessor) {

	ObjectMetaData transition(final ObjectMetaData objectMetaData, final IntrospectionRequest request) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	ObjectMetaDataInitial register(final CausewayBeanMetaData typeMeta) {
		var facetHolder = FacetHolder.simple(mmc(),
                Identifier.classIdentifier(typeMeta.logicalType()));
        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.correspondingClass(), facetHolder);
		var introspectionPolicy = facetHolder.lookupFacet(IntrospectionPolicyFacet.class)
                .map(IntrospectionPolicyFacet::introspectionPolicy)
                .orElseGet(()->mmc().getConfiguration().core().metaModel().introspector().policy());

    	return new ObjectMetaDataInitial(
				typeMeta,
				introspectionPolicy,
				facetHolder);
	}

	ObjectMetaDataTypeOnly typeOnly(
			final ObjectMetaDataInitial registered,
			final FacetedMethodsFactory facetedMethodsFactory,
			final HierarchicalFactory hierarchicalFactory) {

		var facetHolder = registered.facetHolder();
		var typeMeta = registered.typeMeta();
        facetedMethodsFactory.introspectClass();

        // name
        if (facetHolder.lookupFacet(MemberNamedFacet.class).isEmpty()) {
        	facetHolder.addFacet(new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(typeMeta.logicalType().logicalSimpleName()),
                    facetHolder));
        }

        var hierarchical = typeMeta.beanSort().isValue()
        		? Hierarchical.EMPTY
        		: hierarchicalFactory.createHierarchical(typeMeta.correspondingClass());

        return new ObjectMetaDataTypeOnly(
				typeMeta,
				registered.introspectionPolicy(),
				facetHolder,
				facetHolder.lookupFacet(AliasedFacet.class)
					.map(AliasedFacet::getAliases)
					.orElseGet(Can::empty),
				hierarchical);
	}

	ObjectMetaDataFull full(final ObjectMetaDataTypeOnly typeOnly,
			final FacetedMethodsFactory facetedMethodsFactory,
			final MixinSpecStreamer mixinSpecStreamer,
			final PostProcessor postProcessor) {

		var facetHolder = typeOnly.facetHolder();
		var typeMeta = typeOnly.typeMeta();
		var beanSort = typeMeta.beanSort();

		// yet this logic does not skip UNKNONW
        if(beanSort.isCollection()
                || beanSort.isVetoed()
                || beanSort.isValue())
		 return null; //FIXME

        // fully introspect up the type hierarchy including interfaces
        // because members creation depends on presence of inherited members
        typeOnly.hierarchical().streamSuperTypeHierarchyAndInterfaces()
    		.map(ObjectSpecification::correspondingClass)
    		.forEach(cls->specLoaderInternal.loadSpecification(cls, IntrospectionRequest.FULL));

        // create associations and actions

        var regularMemberFactory = new RegularMemberFactory(typeOnly.mixinFacet(), facetedMethodsFactory);
        var regularAssociations = regularMemberFactory.createAssociations().toList();
        var regularActions = regularMemberFactory.createActions().toList();

        ObjectSpecificationInternal spec = null; //TODO refactor
		var mixedInMemberFactory = new MixedInMemberFactory(spec, typeOnly.mixinFacet().isPresent()
        		? MixinSpecStreamer.EMPTY
				: mixinSpecStreamer);
        var mixedInAssociations = mixedInMemberFactory.createMixedInAssociations();
        var mixedInActions = mixedInMemberFactory.createMixedInActions();

        var syntheticActions = mmc().getConfiguration().extensions().commandLog().recordingSupport().isEnabled()
    		? new SyntheticNavigationActionFactory(spec, regularAssociations, mixedInAssociations, regularActions, mixedInActions).synthesizeNavigationActions()
    		: List.<ObjectAction>of();

        var objectAssociationContainer = new AssociationContainer(
        		_MemberSortingUtils.associationsInOrder(spec, regularAssociations, mixedInAssociations),
        		typeOnly.hierarchical().superSpec().orElse(null),
        		spec);
        var objectActionContainer = new ActionContainer(
        		_MemberSortingUtils.actionsInOrder(spec, regularActions, mixedInActions, syntheticActions),
        		ActionScope.forEnvironment(mmc().getSystemEnvironment()),
        		typeOnly.hierarchical().superSpec().orElse(null));

        //TODO? can we run mixin creation without triggering full introspection of other types ... if(!isMixin()) {
		postProcessor.postProcess(spec);
		//}
		var memberCatalog = new MemberCatalog(spec);

		return new ObjectMetaDataFull(
				typeMeta,
				typeOnly.introspectionPolicy(),
				facetHolder,
				typeOnly.hierarchical(),
				objectActionContainer,
				objectAssociationContainer,
				memberCatalog.membersByMethod());
	}

	// -- HELPER

	private MetaModelContext mmc() { return facetProcessor.mmc(); }

}
