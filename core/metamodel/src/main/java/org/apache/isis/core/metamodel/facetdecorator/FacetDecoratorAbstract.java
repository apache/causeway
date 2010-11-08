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


package org.apache.isis.core.metamodel.facetdecorator;

import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;


public abstract class FacetDecoratorAbstract implements FacetDecorator {
    
	public String getFacetTypeNames() {
		Class<? extends Facet>[] decoratorFacetTypes = getFacetTypes();
		StringBuilder buf = new StringBuilder();
        for (int i = 0; i < decoratorFacetTypes.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(decoratorFacetTypes[i].getName());
        }
        return buf.toString();
	}

	protected Facet replaceFacetWithDecoratingFacet(
			final Facet facet, Facet decoratingFacet, FacetHolder requiredHolder) {
		
		// we don't remove, so that the original facet points back to its facet holder
		// (eg a runtime peer object);
		// however, adding the decorating facet means that the required holder points to the 
		// decorating facet rather than the original facet
		
		// holder.removeFacet(facet);
		
		requiredHolder.addFacet(decoratingFacet);
		return decoratingFacet;
	}

}

