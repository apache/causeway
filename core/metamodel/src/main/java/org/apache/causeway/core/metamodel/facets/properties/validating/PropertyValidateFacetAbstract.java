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
package org.apache.causeway.core.metamodel.facets.properties.validating;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.interactions.PropertyModifyContext;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public abstract class PropertyValidateFacetAbstract extends FacetAbstract implements PropertyValidateFacet {

    private static final Class<? extends Facet> type() {
        return PropertyValidateFacet.class;
    }

    public PropertyValidateFacetAbstract(final FacetHolder holder) {
        super(type(), holder);
    }

    public PropertyValidateFacetAbstract(final FacetHolder holder, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
    }

    @Override
    public String invalidates(final ValidityContext context) {
        if (!(context instanceof PropertyModifyContext)) {
            return null;
        }
        final PropertyModifyContext propertyModifyContext = (PropertyModifyContext) context;
        ManagedObject proposed = propertyModifyContext.getProposed();
        if(proposed == null) {
            // skip validation if null value and optional property.
            if(MandatoryFacet.isMandatory(getFacetHolder())) {
                return null;
            }
        }
        return invalidReason(propertyModifyContext.getTarget(), proposed);
    }
}
