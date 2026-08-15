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

import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

record MixinSpecStreamerEager(
		Can<ObjectSpecification> mixinSpecs,
		/** key: mixeeClass, value: mixinSpec */
		_Multimaps.ListMultimap<Class<?>, ObjectSpecification> mixinsByMixeeClass)
implements MixinSpecStreamer {

	MixinSpecStreamerEager(final SpecificationLoader specLoader, final CausewayBeanTypeRegistry beanTypeRegistry) {
		this(beanTypeRegistry.streamMixinTypes()
				.map(specLoader::specForTypeElseFail)
				.filter(mixinSpec-> mixinSpec.mixinFacet().isPresent())
				.collect(Can.toCan()),
				_Multimaps.newListMultimap());
		streamMixinSpecs()
			.forEach(mixinSpec->
				mixinsByMixeeClass.putElement(mixinSpec.mixinFacetElseFail().mixeeType(), mixinSpec));
	}

	@Override
	public Stream<ObjectSpecification> streamMixinSpecs() {
		return mixinSpecs.stream();
	}

	@Override
	public Stream<ObjectSpecification> streamMixinSpecsFor(final ObjectSpecification mixeeSpec) {
		return mixeeSpec.streamTypeHierarchyAndInterfaces()
			.map(ObjectSpecification::correspondingClass)
			.flatMap(mixinsByMixeeClass::streamElements);
	}

}
