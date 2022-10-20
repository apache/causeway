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
package org.apache.causeway.core.metamodel.facets.properties.propertylayout;

import java.util.Optional;

import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacetAbstract;

public class TypicalLengthFacetForPropertyLayoutXml
extends TypicalLengthFacetAbstract {

    public static Optional<TypicalLengthFacet> create(
            final PropertyLayoutData propertyLayout,
            final FacetHolder holder) {
        if(propertyLayout == null) {
            return Optional.empty();
        }
        final Integer typicalLength = propertyLayout.getTypicalLength();
        return typicalLength != null
                && typicalLength != -1
                    ? Optional.of(new TypicalLengthFacetForPropertyLayoutXml(typicalLength, holder))
                    : Optional.empty();
    }

    private TypicalLengthFacetForPropertyLayoutXml(final int typicalLength, final FacetHolder holder) {
        super(typicalLength, holder);
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

}
