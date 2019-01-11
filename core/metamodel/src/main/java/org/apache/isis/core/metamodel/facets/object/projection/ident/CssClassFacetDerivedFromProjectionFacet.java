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

package org.apache.isis.core.metamodel.facets.object.projection.ident;

import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetAbstract2;
import org.apache.isis.core.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class CssClassFacetDerivedFromProjectionFacet extends CssClassFacetAbstract2 {

    private final ProjectionFacet projectionFacet;

    public CssClassFacetDerivedFromProjectionFacet(final ProjectionFacet projectionFacet, final FacetHolder holder) {
        super(holder);
        this.projectionFacet = projectionFacet;
    }

    @Override
    public String cssClass(final ManagedObject targetAdapter) {
        final ObjectAdapter projectedAdapter = projectionFacet.projected(targetAdapter);
        final CssClassFacet cssClassFacet = projectedAdapter.getSpecification().getFacet(CssClassFacet.class);
        return cssClassFacet != null ? cssClassFacet.cssClass(projectedAdapter) : null;
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("projectionFacet", projectionFacet.getClass().getName());
    }

}
