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

package org.apache.isis.valuetypes.sse.metamodel.facets;

import org.apache.isis.valuetypes.sse.applib.annotations.SseSource;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public abstract class SseObserveFacetAbstract extends SingleClassValueFacetAbstract implements SseObserveFacet {

    private Class<? extends SseSource> eventStreamType;

    private static final Class<? extends Facet> type() {
        return SseObserveFacet.class;
    }

    public SseObserveFacetAbstract(
            final Class<? extends SseSource> eventStreamType,
            final FacetHolder holder) {

        super(type(), holder, eventStreamType);
        this.eventStreamType = eventStreamType;
    }

    @Override
    public Class<?> value() {
        return eventStreamType;
    }

    @Override
    public Class<? extends SseSource> getEventStreamType() {
        return eventStreamType;
    }

    @Override
    public ObjectSpecification valueSpec() {
        throw _Exceptions.notImplemented();
    }


}
