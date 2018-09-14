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

package org.apache.isis.core.metamodel.facets.properties.property.hidden;

import java.util.List;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstract;

public class HiddenFacetForPropertyAnnotation extends HiddenFacetAbstract {

    public static HiddenFacet create(
            final List<Property> properties,
            final FacetHolder holder) {

        return properties.stream()
                .map(Property::hidden)
                .filter(where -> where != null && where != Where.NOT_SPECIFIED)
                .findFirst()
                .map(where -> new HiddenFacetForPropertyAnnotation(where, holder))
                .orElse(null);
    }

    private HiddenFacetForPropertyAnnotation(final Where where, final FacetHolder holder) {
        super(HiddenFacetForPropertyAnnotation.class, where, holder);
    }

    @Override
    public String hiddenReason(final ObjectAdapter targetAdapter, final Where whereContext) {
        if(!where().includes(whereContext)) {
            return null;
        }
        return "Hidden on " + where().getFriendlyName();
    }

}
