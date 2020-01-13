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


package org.apache.isis.core.metamodel.examples.facets.jsr303;

import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;
import org.apache.isis.core.metamodel.specloader.progmodelfacets.ProgrammingModelFacetsJava5;


/**
 * Implementation of {@link ProgrammingModelFacets} that additionally just installs support for
 * {@link Jsr303PropertyValidationFacet JSR-303} validation.
 * 
 * <p>
 * This implementation is really provided only as an example. Typically you would provide your own
 * {@link ProgrammingModelFacets} implementation that installs other additional {@link FacetFactory facet
 * factories} ass required for your programming model.
 */
public class Jsr303ProgModelFacets extends ProgrammingModelFacetsJava5 {

	
	public Jsr303ProgModelFacets() {
		addFactory(Jsr303FacetFactory.class);
	}

}
