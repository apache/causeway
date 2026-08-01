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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.debug._Debug.Profiler;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

record MixedInMemberFactory(
		ObjectSpecification spec,
		MixinSpecStreamer mixinSpecStreamer) {

	/**
     * Creates all mixed in properties and collections for this spec.
	 * @param profiler
     */
    public List<ObjectAssociation> createMixedInAssociations(final Profiler profiler) {

    	var include =

    	profiler.measure("members.mixedInAssociations.createMixedInAssociation.inclusion", ()->

    	spec.isEntityOrViewModelOrAbstract()
    			&& !spec.isInjectable()
    			&& !spec.isValue()
    	);

        return include
    		? profiler.measure("members.mixedInAssociations.createMixedInAssociation.stream", ()->
    				mixinSpecStreamer.streamMixinSpecs()
				.filter(mixinSpec-> mixinSpec != spec)
	            .flatMap(this::createMixedInAssociation)
	            .toList())
            : List.of();
    }

    /**
     * Creates all mixed in actions for this spec.
     */
    public List<ObjectActionMixedIn> createMixedInActions() {
        var include = spec.isEntityOrViewModelOrAbstract()
                || spec.beanSort().isManagedBeanContributing()
                // in support of composite value-type constructor mixins
                || spec.beanSort().isValue();
        return include
    		? mixinSpecStreamer.streamMixinSpecs()
				.filter(mixinSpec-> mixinSpec != spec)
				.flatMap(this::createMixedInAction)
				.toList()
			: List.of();
    }

    // -- HELPER

    private Stream<ObjectAssociation> createMixedInAssociation(final ObjectSpecification mixinSpec) {
		var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null)
			// this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        if(!mixinFacet.isMixinFor(spec.getCorrespondingClass()))
			return Stream.empty();
        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
	        .filter(_SpecPredicates::isMixedInAssociation)
	        .map(ObjectActionDefault.class::cast)
	        .map(mixedInAssociation(spec, mixinSpec, mixinFacet.getMainMethodName()));
    }

    private Stream<ObjectActionMixedIn> createMixedInAction(final ObjectSpecification mixinSpec) {
        var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null)
			// this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        if(!mixinFacet.isMixinFor(spec.getCorrespondingClass()))
			return Stream.empty();
        // don't mixin Object_ mixins to domain services
        if(spec.beanSort().isManagedBeanContributing()
                && mixinFacet.isMixinFor(java.lang.Object.class))
			return Stream.empty();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
	        // value types only support constructor mixins
	        .filter(this::whenIsValueThenIsAlsoConstructorMixin)
	        .filter(_SpecPredicates::isMixedInAction)
	        .map(ObjectActionDefault.class::cast)
	        .map(mixedInAction(spec, mixinSpec, mixinFacet.getMainMethodName()));
    }

    /**
     * Whether the mixin's main method returns an instance of type equal to the mixee's type.
     * <p>
     * Introduced to support constructor mixins for value-types and
     * also to support associated <i>Actions</i> for <i>Action Parameters</i>.
     */
    private boolean whenIsValueThenIsAlsoConstructorMixin(final ObjectAction act) {
        return spec.beanSort().isValue()
                ? Objects.equals(spec, act.getReturnType())
                : true;
    }

    private static Function<ObjectActionDefault, ObjectActionMixedIn> mixedInAction(
            final ObjectSpecification mixeeSpec,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName) {

        return mixinAction -> new ObjectActionMixedIn(
                mixinSpec, mixinMethodName, mixinAction, mixeeSpec);
    }

    private static Function<ObjectActionDefault, ObjectAssociation> mixedInAssociation(
            final ObjectSpecification mixeeSpec,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName) {

        return mixinAction -> mixinAction.getReturnType().isSingular()
                ? new OneToOneAssociationMixedIn(
                        mixeeSpec, mixinAction, mixinSpec, mixinMethodName)
                : new OneToManyAssociationMixedIn(
                        mixeeSpec, mixinAction, mixinSpec, mixinMethodName);
    }

}
