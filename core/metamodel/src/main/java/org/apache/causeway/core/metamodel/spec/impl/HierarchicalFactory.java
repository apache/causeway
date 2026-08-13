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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.jspecify.annotations.Nullable;

record HierarchicalFactory(
		SpecificationLoaderInternal specLoaderInternal,
		ClassSubstitutorRegistry classSubstitutorRegistry,
		/** used for validation messages */
		FacetHolder facetHolder) {

	Hierarchical createHierarchical(final @Nullable Class<?> cls) {
		return cls!=null
			? new HierarchicalRecord
				(Optional.ofNullable(cls.getSuperclass())
					.map(specLoaderInternal::loadSpecificationTypeOnly),
				loadInterfaces(cls))
			: Hierarchical.EMPTY;
	}

	// -- HELPER

	private record HierarchicalRecord(
			Optional<ObjectSpecification> superSpec,
			Can<ObjectSpecification> interfaceSpecs)
	implements Hierarchical {

		@Override
		public Can<ObjectSpecification> interfaces() {
			return interfaceSpecs;
		}

		@Override
		public ObjectSpecification superclass() {
			return superSpec.orElse(null);
		}

	}

    private Can<ObjectSpecification> loadInterfaces(final Class<?> cls) {
    	final Class<?>[] interfaces = cls.getInterfaces();

    	if(interfaces==null)
			return Can.empty();

    	var classCache = _ClassCache.getInstance();

        final List<ObjectSpecification> interfaceSpecList = Stream.of(interfaces)
    		// pre-filter common interfaces (performance)
        	.filter(interfaceType->!interfaceType.getName().startsWith("java."))
        	//--
        	.map(interfaceType->{
        		var substitution = classSubstitutorRegistry.getSubstitution(interfaceType);
                return substitution.isReplace()
                		? substitution.replacement()
        				: substitution.isNeverIntrospect()
    	    				? null
    	    				: interfaceType;
        	})
        	.filter(Objects::nonNull)
        	.filter(interfaceType->classCache.head(interfaceType).hasAnnotation(DomainObject.class))
        	.map(specLoaderInternal::loadSpecificationTypeOnly)
        	.filter(Objects::nonNull)
        	.toList();

        if(interfaceSpecList.isEmpty())
        	return Can.empty();

        if(interfaceSpecList.size()>1) {
        	ValidationFailure.raiseFormatted(facetHolder,
        			"Cannot use @DomainObject on more than one interface, as inherited by: %s",
        			cls.getName());
        }
        if (cls.getSuperclass() != null) {
        	if(classCache.head(cls.getSuperclass()).hasAnnotation(DomainObject.class)) {
        		ValidationFailure.raiseFormatted(facetHolder,
        				"Cannot use @DomainObject on both, abstract super class and one interface, as inherited by: %s",
        				cls.getName());
        	}
        }

        return Can.ofCollection(interfaceSpecList);
    }

}
