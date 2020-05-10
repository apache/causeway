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

package org.apache.isis.core.metamodel.facets.properties.validating.dflt;

import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.validating.PropertyValidateFacet;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Non checking property validation facet that provides default behaviour for
 * all properties.
 */
public class PropertyValidateFacetDefault extends FacetAbstract implements PropertyValidateFacet {

    @Override
    public String invalidates(final ValidityContext ic) {
        return null;
    }

    public PropertyValidateFacetDefault(final FacetHolder holder) {
        super(PropertyValidateFacet.class, holder, Derivation.NOT_DERIVED);
    }

    @Override
    public String invalidReason(final ManagedObject target, final ManagedObject proposedValue) {
        return null;
    }

}
