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

package org.apache.isis.core.metamodel.facets.object.callbacks;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;

public class CreatedLifecycleEventFacetForDomainObjectAnnotation extends SingleClassValueFacetAbstract implements CreatedLifecycleEventFacet {

    static Class<CreatedLifecycleEventFacet> type() {
        return CreatedLifecycleEventFacet.class;
    }

    public CreatedLifecycleEventFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final Class<? extends ObjectCreatedEvent<?>> value) {
        super(type(), holder, value);
    }

    @Override
    public Class<? extends AbstractLifecycleEvent<?>> getEventType() {
        return _Casts.uncheckedCast(value());
    }


}
