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

package org.apache.isis.metamodel.facets.object.domainobject.domainevents;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;

/**
 * This does <i>NOT</i> implement {@link ActionDomainEventFacet}, rather it is to record the default type to use
 * for any actions as a fallback/default.
 */
public class ActionDomainEventDefaultFacetForDomainObjectAnnotation
                    extends SingleClassValueFacetAbstract  {


    private final Class<? extends ActionDomainEvent<?>> eventType;
    public Class<? extends ActionDomainEvent<?>> getEventType() {
        return eventType;
    }

    static Class<? extends Facet> type() {
        return ActionDomainEventDefaultFacetForDomainObjectAnnotation.class;
    }

    public ActionDomainEventDefaultFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final Class<? extends ActionDomainEvent<?>> value) {
        super(type(), holder, value);
        this.eventType = value;
    }

}
