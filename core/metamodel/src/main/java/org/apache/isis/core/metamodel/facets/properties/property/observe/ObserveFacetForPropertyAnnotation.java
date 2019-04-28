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

package org.apache.isis.core.metamodel.facets.properties.property.observe;

import java.util.List;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.sse.EventStreamSource;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.observe.ObserveFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.observe.ObserveFacetAbstract;

/**
 * 
 * @since 2.0.0-M3
 *
 */
public class ObserveFacetForPropertyAnnotation extends ObserveFacetAbstract {

    public static ObserveFacet create(
            final List<Property> properties,
            final FacetHolder holder) {

        return properties.stream()
                .map(Property::observe)
                .filter(EventStreamSource::isObservable)
                .findFirst()
                .map(eventStreamType -> new ObserveFacetForPropertyAnnotation(
                        eventStreamType, holder))
                .orElse(null);
    }

    private ObserveFacetForPropertyAnnotation(
            Class<? extends EventStreamSource> eventStreamType, 
            FacetHolder holder) {
        
        super(eventStreamType, holder);
    }
    

}
