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

package org.apache.isis.core.metamodel.facets.properties.defaults.fromtype;

import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;

public class PropertyDefaultFacetDerivedFromDefaultedFacet extends FacetAbstract implements PropertyDefaultFacet {

    private final DefaultedFacet typeFacet;
    private final AdapterManager adapterManager;

    public PropertyDefaultFacetDerivedFromDefaultedFacet(final DefaultedFacet typeFacet, final FacetHolder holder, final AdapterManager adapterManager) {
        super(PropertyDefaultFacet.class, holder, Derivation.NOT_DERIVED);
        this.typeFacet = typeFacet;
        this.adapterManager = adapterManager;
    }

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter inObject) {
        if (getIdentified() == null) {
            return null;
        }
        final Object typeFacetDefault = typeFacet.getDefault();
        if (typeFacetDefault == null) {
            return null;
        }
        return getAdapterManager().adapterFor(typeFacetDefault);
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("typeFacet", typeFacet);
    }
}
