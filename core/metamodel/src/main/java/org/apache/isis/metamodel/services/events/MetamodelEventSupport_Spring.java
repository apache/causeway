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
package org.apache.isis.metamodel.services.events;

import javax.enterprise.event.Event;
import javax.inject.Named;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.applib.events.ui.TitleUiEvent;
import org.apache.isis.commons.internal.ioc.spring._Spring;

@Configuration
@Named("isisMetaModel.metamodelEventSupport_Spring")
public class MetamodelEventSupport_Spring {

    @Bean
    public Event<CssClassUiEvent<Object>> cssClassUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<IconUiEvent<Object>> iconUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<LayoutUiEvent<Object>> layoutUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<TitleUiEvent<Object>> titleUiEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<ActionDomainEvent<?>> actionDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<PropertyDomainEvent<?, ?>> propertyDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }

    @Bean
    public Event<CollectionDomainEvent<?, ?>> collectionDomainEvents(ApplicationEventPublisher publisher) {
        return _Spring.event(publisher);
    }


}
