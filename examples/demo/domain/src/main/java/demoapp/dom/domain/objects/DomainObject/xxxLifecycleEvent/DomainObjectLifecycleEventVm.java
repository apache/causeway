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
package demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.jaxb.JavaTimeJaxbAdapters.LocalDateTimeToStringAdapter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

import java.time.LocalDateTime;

//tag::class[]
@XmlRootElement(name = "demo.DomainObjectLifecycleEventVm")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectLifecycleEventVm")
@DomainObject(nature = Nature.VIEW_MODEL)
@NoArgsConstructor
public class DomainObjectLifecycleEventVm implements HasAsciiDocDescription {

    public DomainObjectLifecycleEventVm(LocalDateTime timestamp, String eventType, String bookmark) {
        this.eventType = eventType;
        this.bookmark = bookmark;
        this.timestamp = timestamp;
    }

    @ObjectSupport public String title() {
        return timestamp + ":" + eventType + " on " + bookmark;
    }

    @Property
    @XmlJavaTypeAdapter(LocalDateTimeToStringAdapter.class)
    @Getter @Setter
    private LocalDateTime timestamp;

    @Property
    @Getter @Setter
    private String eventType;

    @Property
    @Getter @Setter
    private String bookmark;

}
//end::class[]
