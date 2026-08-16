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
package org.apache.causeway.core.metamodel.object;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.jspecify.annotations.Nullable;

/**
 * Introduced for debugging.
 */
public final class Mm2YamlUtils {

	public static String toYaml(final Iterable<? extends ObjectSpecification> objSpecs) {
		var model = new Model();
		objSpecs.forEach(model::collect);
		return model.toYaml();
	}

	public static String toYaml(final Stream<? extends ObjectSpecification> objSpecs) {
		var model = new Model();
		objSpecs.forEach(model::collect);
		return model.toYaml();
	}

	// -- HELPER

	private record Model(
			List<ObjectSpecification> mixinSpecs,
			List<ObjectSpecification> valueSpecs,
			List<ObjectSpecification> serviceSpecs,
			List<ObjectSpecification> entitySpecs,
			List<ObjectSpecification> vmSpecs,
			List<ObjectSpecification> abstractSpecs,
			List<ObjectSpecification> otherSpecs) {

		private record Writer(StringBuilder sb) {
			Writer() {
				this(new StringBuilder());
			}
			void writeln(final String line) {
				sb.append(line).append("\n");
			}
			void writeln(final String format, final Object ...args) {
				writeln(format.formatted(args));
			}
			@Override
			public final String toString() {
				return sb.toString();
			}
		}

		Model() {
			this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}

		void collect(final ObjectSpecification objSpec) {
			switch (objSpec.beanSort()) {
				case MIXIN -> mixinSpecs.add(objSpec);
				case VALUE -> valueSpecs.add(objSpec);
				case MANAGED_BEAN_CONTRIBUTING, MANAGED_BEAN_NOT_CONTRIBUTING -> serviceSpecs.add(objSpec);
				case ENTITY  -> entitySpecs.add(objSpec);
				case VIEW_MODEL -> vmSpecs.add(objSpec);
				case ABSTRACT  -> abstractSpecs.add(objSpec);
				case COLLECTION, PROGRAMMATIC, UNKNOWN, VETOED -> otherSpecs.add(objSpec);
			};
		}

		String toYaml() {
			var writer = new Writer();
			category(writer, "Mixins", mixinSpecs);
			category(writer, "Values", valueSpecs);
			category(writer, "Services", serviceSpecs);
			category(writer, "Entities", entitySpecs);
			category(writer, "Viewmodels", vmSpecs);
			category(writer, "Abstract", abstractSpecs);
			category(writer, "Other", otherSpecs);
			return writer.toString();
		}

		private void category(final Writer writer, final String name, final List<ObjectSpecification> specs) {
			writer.writeln("%s (count=%d):", name, specs.size());
			specs.stream().sorted()
				.forEach(spec->writer.writeln("- {class=%s%s, ract={%s}, rass={%s}}",
						spec.fullIdentifier(),
						formatSuper(spec.superSpec().orElse(null)),
						formatRegularActions(spec),
						formatRegularAssociations(spec)
						));
		}

		private String formatSuper(@Nullable final ObjectSpecification spec) {
			return spec == null
					|| spec.correspondingClass().equals(java.lang.Record.class)
					|| spec.correspondingClass().equals(java.lang.Object.class)
	                ? ""
	                : ", super=" + spec.fullIdentifier();
		}

		private String formatRegularActions(final ObjectSpecification spec) {
			return spec.streamRuntimeActions(MixedIn.EXCLUDED)
					.map(ObjectAction::getId)
					.collect(Collectors.joining(", "));
		}

		private String formatRegularAssociations(final ObjectSpecification spec) {
			return spec.streamAssociations(MixedIn.EXCLUDED)
					.map(ObjectAssociation::getId)
					.collect(Collectors.joining(", "));
		}

	}

}
