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

package org.apache.isis.metamodel.facets.properties.propertylayout;

import java.util.List;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderDay;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacetAbstract;

public class RenderedAdjustedFacetForPropertyLayoutAnnotation extends RenderedAdjustedFacetAbstract {

    public static RenderedAdjustedFacet create(
            final List<PropertyLayout> propertyLayouts,
            final FacetHolder holder) {

        return propertyLayouts.stream()
                .map(PropertyLayout::renderDay)
                .filter(renderDay -> renderDay != RenderDay.NOT_SPECIFIED)
                .findFirst()
                .map(renderDay -> {
                    switch (renderDay) {
                    case AS_DAY:
                        return null;
                    case AS_DAY_BEFORE:
                        return new RenderedAdjustedFacetForPropertyLayoutAnnotation(holder);
                    default:
                    }
                    throw new IllegalStateException("renderDay '" + renderDay + "' not recognised");
                })
                .orElse(null);
    }

    public static final int ADJUST_BY = -1;

    private RenderedAdjustedFacetForPropertyLayoutAnnotation(FacetHolder holder) {
        super(ADJUST_BY, holder);
    }

}
