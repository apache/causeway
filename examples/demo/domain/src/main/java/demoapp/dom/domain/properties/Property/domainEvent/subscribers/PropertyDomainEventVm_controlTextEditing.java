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
package demoapp.dom.domain.properties.Property.domainEvent.subscribers;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.properties.Property.domainEvent.PropertyDomainEventVm;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
    promptStyle = PromptStyle.INLINE_AS_IF_EDIT
    , redirectPolicy = Redirect.EVEN_IF_SAME                        // <.>
    , associateWith = "controlText"
    , sequence = "1"
)
@RequiredArgsConstructor
public class PropertyDomainEventVm_controlTextEditing {

    private final PropertyDomainEventVm propertyDomainEventVm;

    @MemberSupport public PropertyDomainEventVm act(final PropertyDomainEventControlStrategy controlStrategy) {
        eventControlService.controlStrategy = controlStrategy;
        return propertyDomainEventVm;
    }
    @MemberSupport public PropertyDomainEventControlStrategy default0Act() {
        return eventControlService.controlStrategy;
    }

    @Inject
    PropertyDomainEventControlService eventControlService;
}
//end::class[]
