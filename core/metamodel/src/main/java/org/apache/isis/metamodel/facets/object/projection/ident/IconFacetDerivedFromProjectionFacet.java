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

package org.apache.isis.metamodel.facets.object.projection.ident;

import java.util.Map;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.icon.IconFacetAbstract;
import org.apache.isis.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.isis.metamodel.spec.ManagedObject;

public class IconFacetDerivedFromProjectionFacet extends IconFacetAbstract {

    private final ProjectionFacet projectionFacet;

    public IconFacetDerivedFromProjectionFacet(final ProjectionFacet projectionFacet, final FacetHolder holder) {
        super(holder);
        this.projectionFacet = projectionFacet;
    }

    @Override
    public String iconName(final ManagedObject targetAdapter) {
        final ObjectAdapter projectedAdapter = projectionFacet.projected(targetAdapter);
        return projectedAdapter.getSpecification().getIconName(projectedAdapter);
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("projectionFacet", projectionFacet.getClass().getName());
    }

}
