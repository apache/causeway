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
package demoapp.dom.types.jodatime.jodalocaldatetime.vm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters;
import org.apache.isis.applib.jaxb.JodaTimeJaxbAdapters;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.jodatime.jodalocaldatetime.holder.JodaLocalDateTimeHolder3;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.JodaLocalDateTimeVm"
)
@lombok.NoArgsConstructor                                                       // <.>
public class JodaLocalDateTimeVm
        implements HasAsciiDocDescription, JodaLocalDateTimeHolder3 {

//end::class[]
    public JodaLocalDateTimeVm(org.joda.time.LocalDateTime initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "org.joda.time.LocalDateTime view model: ")
    @MemberOrder(name = "read-only-properties", sequence = "1")
    @XmlElement(required = true)                                                // <.>
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateTimeToStringAdapter.class)                    // <.>
    @Getter @Setter
    private org.joda.time.LocalDateTime readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @MemberOrder(name = "editable-properties", sequence = "1")
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateTimeToStringAdapter.class)
    @Getter @Setter
    private org.joda.time.LocalDateTime readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @MemberOrder(name = "optional-properties", sequence = "1")
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateTimeToStringAdapter.class)
    @Getter @Setter
    private org.joda.time.LocalDateTime readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @MemberOrder(name = "optional-properties", sequence = "2")
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateTimeToStringAdapter.class)
    @Getter @Setter
    private org.joda.time.LocalDateTime readWriteOptionalProperty;

}
//end::class[]
