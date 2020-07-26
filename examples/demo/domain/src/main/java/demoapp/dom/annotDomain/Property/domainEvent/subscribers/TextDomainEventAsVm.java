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
package demoapp.dom.annotDomain.Property.domainEvent.subscribers;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdo;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.TextDomainEventAsVm"
)
@NoArgsConstructor
public class TextDomainEventAsVm {

    public TextDomainEventAsVm(PropertyDomainEventJdo.TextDomainEvent ev) {
        this.phase = ev.getEventPhase();
        // this.subject = (PropertyDomainEventJdo) ev.getSubject();
        this.hidden = ev.isHidden();
        this.oldValue = ev.getOldValue();
        this.newValue = ev.getOldValue();
        this.disabledReason = ev.getDisabledReason();
        this.invalidityReason = ev.getInvalidityReason();
    }

    @Getter @Setter
    private AbstractDomainEvent.Phase phase;

    @Getter @Setter
    private PropertyDomainEventJdo subject;

    @Getter @Setter
    private String oldValue;
    @Getter @Setter
    private String newValue;

    @Getter @Setter
    private boolean hidden;
    @Getter @Setter
    private String disabledReason;
    @Getter @Setter
    private String invalidityReason;



}
//end::class[]
