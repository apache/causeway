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


package org.apache.isis.core.metamodel.java5;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.commons.filters.AbstractFilter;
import org.apache.isis.core.metamodel.facets.Facet;

/**
 * Finds the {@link ImperativeFacet}(s) that correspond to the provided method. 
 */
public final class ImperativeFacetsForMethodFilter extends AbstractFilter<Facet> {
	private final Method method;

	ImperativeFacetsForMethodFilter(Method method) {
		this.method = method;
	}

	public boolean accept(Facet facet) {
		ImperativeFacet imperativeFacet = 
			ImperativeFacetUtils.getImperativeFacet(facet);
		if (imperativeFacet == null) {
			return false;
		}
		List<Method> methods = imperativeFacet.getMethods();
		for(Method method: methods) {
			// Method has value semantics
			if (method.equals(this.method)) {
				return true;
			}
		}
		return false;
	}
}