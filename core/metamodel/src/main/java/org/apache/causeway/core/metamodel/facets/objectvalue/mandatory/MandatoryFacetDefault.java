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
package org.apache.causeway.core.metamodel.facets.objectvalue.mandatory;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

/**
 * Whether a property or a parameter is mandatory (not optional).
 *
 * <p>
 * For a mandatory property, the object cannot be saved/updated without the
 * value being provided. For a mandatory parameter, the action cannot be invoked
 * without the value being provided.
 */
public class MandatoryFacetDefault extends MandatoryFacetAbstract {

    public static MandatoryFacetDefault required(final FacetHolder holder) {
        return new MandatoryFacetDefault(holder, Semantics.REQUIRED);
    }

    private MandatoryFacetDefault(final FacetHolder holder, final Semantics semantics) {
        // unconditionally created, hence acting as a fallback
        super(holder, semantics, Facet.Precedence.FALLBACK);
    }

}
