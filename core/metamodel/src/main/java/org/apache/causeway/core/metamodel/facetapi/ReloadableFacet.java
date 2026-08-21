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
package org.apache.causeway.core.metamodel.facetapi;

/**
 * Enables reloading for the implementing {@link Facet}. Cooperates with {@link FacetAbstract}'s constructor.
 *
 * @see FacetAbstract
 * @see _BindUtil#purgeExactFacetClassHonoringPrecedence
 * @since 4.0
 */
public interface ReloadableFacet {

	/**
	 * WARNING: Meant to be called by {@link FacetAbstract} only!
	 *
     * @apiNote this is supposed to be called during {@link FacetAbstract} construction,
     * 		meaning this {@link Facet} might not be fully initialized yet!
     *
     * @see FacetAbstract
     * @see _BindUtil#purgeExactFacetClassHonoringPrecedence
     */
    default void onPrebind(final FacetHolder facetHolder) {
    	if(this instanceof FacetAbstract facetAbstract) {
    		var key = QualifiedFacet.Key.forFacet(facetAbstract); // requires qualifier to be initialized already!
    		// dynamic update support
    		_BindUtil.purgeExactFacetClassHonoringPrecedence(facetAbstract.getClass(),
    				key, facetHolder, facetAbstract.precedence(), key.qualifier());
    	}
 	}

}
