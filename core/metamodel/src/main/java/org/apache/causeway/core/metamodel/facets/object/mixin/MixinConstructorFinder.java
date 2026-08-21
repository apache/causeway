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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

record MixinConstructorFinder(
		/** used for validation messages */
		FacetHolder facetHolder) {

	final static String ANNOTATION_LITERAL = "@DomainObject#nature=MIXIN";

	Optional<ResolvedConstructor> findConstructor(final Class<?> mixinType) {
		return mixinType.isRecord()
			? findConstructorsMultiArgSupported(mixinType)
			: findConstructors1ArgEnforced(mixinType);
	}

	private Optional<ResolvedConstructor> findConstructors1ArgEnforced(final Class<?> mixinType) {
		var mixinContructors =
	            ProgrammingModelConstants.MixinConstructor.PUBLIC_SINGLE_ARG_RECEIVING_MIXEE
	                .getAll(mixinType);

        if(mixinContructors.getCardinality().isOne())
        	return mixinContructors.getSingleton(); // happy case

        if(mixinContructors.getCardinality().isZero()) {
            ValidationFailure.raise(
                    facetHolder.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(mixinType)),
                    String.format(
                        "%s: annotated with %s annotation but does not have a public 1-arg constructor",
                        mixinType.getName(),
                        ANNOTATION_LITERAL)
                    );
        } else {
            ValidationFailure.raise(
                    facetHolder.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(mixinType)),
                    String.format(
                            "%s: annotated with %s annotation needs a single public 1-arg constructor but has %d",
                            mixinType.getName(),
                            ANNOTATION_LITERAL,
                            mixinContructors.size())
                    );
        }
        return Optional.empty();
	}

	/**
	 * multiple args allowed in constructor, same as for implementations of {@link ViewModel}
	 */
	private Optional<ResolvedConstructor> findConstructorsMultiArgSupported(final Class<?> mixinType) {
        var explicitInjectConstructors = ProgrammingModelConstants.MixinConstructor.PUBLIC_WITH_INJECT_SEMANTICS.getAll(mixinType);
        var publicConstructors = ProgrammingModelConstants.MixinConstructor.PUBLIC_ANY.getAll(mixinType);

        var violation = explicitInjectConstructors.getCardinality().isMultiple()
                ? ProgrammingModelConstants.MessageTemplate.MIXIN_MULTIPLE_CONSTRUCTORS_WITH_INJECT_SEMANTICS
                : explicitInjectConstructors.getCardinality().isZero()
                    && !publicConstructors.getCardinality().isOne()
                        // in absence of a constructor with inject semantics there must be exactly one public to pick instead
                        ? ProgrammingModelConstants.MessageTemplate.MIXIN_MISSING_OR_MULTIPLE_PUBLIC_CONSTRUCTORS
                        : null;

        if(violation!=null) {

            ValidationFailure.raiseFormatted(facetHolder,
                    violation
                        .builder()
                        .addVariable("type", mixinType.getName())
                        .addVariable("found", explicitInjectConstructors.getCardinality().isMultiple()
                                ? "{" + explicitInjectConstructors.stream()
                                        .map(ResolvedConstructor::constructor)
                                        .map(Constructor::toString)
                                        .collect(Collectors.joining(", ")) + "}"
                                : "none")
                        .buildMessage());

            return Optional.empty();

        }

        // -- else happy case

        return explicitInjectConstructors.getCardinality().isOne()
                ? explicitInjectConstructors.getSingleton()
                : publicConstructors.getSingleton();
	}

}
