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
package org.apache.causeway.core.interaction.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureFilter;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Multimaps;

record ApplicationFeatureFilters(
		Can<ApplicationFeatureFilter> unqualifiedFilters,
		Map<String, Can<ApplicationFeatureFilter>> filtersByQualifier) {

	// -- FACTORIES

	static ApplicationFeatureFilters empty() {
		return new ApplicationFeatureFilters(Can.empty(), Collections.emptyMap());
	}

	static ApplicationFeatureFilters collectFrom(final @Nullable ApplicationContext springContext) {
		if(springContext==null)
			//JUnit Support
			return ApplicationFeatureFilters.empty();

		var unqualifiedFilters = new ArrayList<ApplicationFeatureFilter>();
		var filtersByQualifier = _Multimaps.<String, ApplicationFeatureFilter>newListMultimap();

        Stream.of(springContext.getBeanNamesForType(ApplicationFeatureFilter.class))
    	.forEach(beanName->{
    		var filterBean = springContext.getBean(beanName, ApplicationFeatureFilter.class);
            @SuppressWarnings("deprecation")
			Set<Qualifier> qualifiers = AnnotationUtils.getRepeatableAnnotations(filterBean.getClass(), Qualifier.class);
            if(!_NullSafe.isEmpty(qualifiers)) {
            	qualifiers.forEach(qualifier->{
            		if(StringUtils.hasText(qualifier.value())) {
            			filtersByQualifier.putElement(qualifier.value(), filterBean);
            		} else {
            			unqualifiedFilters.add(filterBean);
            		}
            	});
            } else {
            	unqualifiedFilters.add(filterBean);
            }
    	});

        // Sanitize (no duplicates, when already in unqualified Can) and make immutable
        var unqualifiedFiltersCanned = Can.ofCollection(unqualifiedFilters).distinct();
		var filtersByQualifierCanned = new HashMap<String, Can<ApplicationFeatureFilter>>();
		filtersByQualifier.forEach((k, v)->filtersByQualifierCanned.put(k, Can.ofCollection(v).distinct().filter(it->!unqualifiedFiltersCanned.contains(it))));
		return new ApplicationFeatureFilters(
				unqualifiedFiltersCanned,
				Collections.unmodifiableMap(filtersByQualifierCanned));
	}

}
