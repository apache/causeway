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
package org.apache.causeway.core.metamodel.services.events;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.events.ui.CssClassUiEvent;
import org.apache.causeway.applib.events.ui.IconUiEvent;
import org.apache.causeway.applib.events.ui.LayoutUiEvent;
import org.apache.causeway.applib.events.ui.TitleUiEvent;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".MetamodelEventService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class MetamodelEventService {

    private final ApplicationEventPublisher publisher;

    // -- METAMODEL UI EVENTS

    public void fireCssClassUiEvent(final CssClassUiEvent<Object> event) {
        publisher.publishEvent(event);
    }

    public void fireIconUiEvent(final IconUiEvent<Object> event) {
        publisher.publishEvent(event);
    }

    public void fireLayoutUiEvent(final LayoutUiEvent<Object> event) {
        publisher.publishEvent(event);
    }

    public void fireTitleUiEvent(final TitleUiEvent<Object> event) {
        publisher.publishEvent(event);
    }

    public void fireActionDomainEvent(final ActionDomainEvent<?> event) {
        publisher.publishEvent(event);
    }

    public void firePropertyDomainEvent(final PropertyDomainEvent<?, ?> event) {
        publisher.publishEvent(event);
    }

    public void fireCollectionDomainEvent(final CollectionDomainEvent<?, ?> event) {
        publisher.publishEvent(event);
    }

}
