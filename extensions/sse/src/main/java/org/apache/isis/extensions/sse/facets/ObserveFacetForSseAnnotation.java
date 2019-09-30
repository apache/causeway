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

package org.apache.isis.extensions.sse.facets;

import java.util.List;

import org.apache.isis.extensions.sse.api.SseSource;
import org.apache.isis.extensions.sse.api.ServerSentEvents;
import org.apache.isis.metamodel.facetapi.FacetHolder;

/**
 * 
 * @since 2.0
 *
 */
public class ObserveFacetForSseAnnotation extends ObserveFacetAbstract {

    public static ObserveFacet create(
            final List<ServerSentEvents> properties,
            final FacetHolder holder) {

        return properties.stream()
                .map(ServerSentEvents::observe)
                .filter(SseSource::isObservable)
                .findFirst()
                .map(eventStreamType -> new ObserveFacetForSseAnnotation(
                        eventStreamType, holder))
                .orElse(null);
    }

    private ObserveFacetForSseAnnotation(
            Class<? extends SseSource> eventStreamType, 
            FacetHolder holder) {

        super(eventStreamType, holder);
    }


}
