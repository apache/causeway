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

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public interface FacetDecorator {

    /**
     * If applicable, replace the provided {@link Facet} with a decorating
     * {@link Facet} that should be held by the provided {@link FacetHolder
     * required holder}.
     * 
     * <p>
     * 
     * @return the decorating {@link Facet}, or <tt>null</tt> if this decoration
     *         does not apply to this Facet.
     */
    Facet decorate(Facet facet, FacetHolder requiredHolder);

    Class<? extends Facet>[] getFacetTypes();

    String getFacetTypeNames();

}
