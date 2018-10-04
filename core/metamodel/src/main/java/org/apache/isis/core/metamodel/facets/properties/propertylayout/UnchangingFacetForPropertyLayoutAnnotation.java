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

package org.apache.isis.core.metamodel.facets.properties.propertylayout;

import java.util.List;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Repainting;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacetAbstract;

public class UnchangingFacetForPropertyLayoutAnnotation extends UnchangingFacetAbstract {

    public static UnchangingFacet create(
            final List<PropertyLayout> propertyLayouts,
            final FacetHolder holder) {

        return propertyLayouts.stream()
                .map(PropertyLayout::repainting)
                .filter(repainting -> repainting != Repainting.NOT_SPECIFIED)
                .findFirst()
                .map(repainting -> {
                    boolean unchanging;
                    switch (repainting) {
                    case REPAINT:
                        unchanging = false;
                        return new UnchangingFacetForPropertyLayoutAnnotation(unchanging, holder);
                    case NO_REPAINT:
                        unchanging = true;
                        return new UnchangingFacetForPropertyLayoutAnnotation(unchanging, holder);
                    default:
                    }
                    throw new IllegalStateException("repainting '" + repainting + "' not recognised");
                })
                .orElse(null);
    }

    private UnchangingFacetForPropertyLayoutAnnotation(final boolean unchanging, FacetHolder holder) {
        super(unchanging, holder);
    }

}
