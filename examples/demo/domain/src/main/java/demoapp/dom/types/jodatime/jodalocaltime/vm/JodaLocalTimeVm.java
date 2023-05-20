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
package demoapp.dom.types.jodatime.jodalocaltime.vm;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;

import org.joda.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.jodatime.jodalocaltime.holder.JodaLocalTimeHolder2;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.JodaLocalTimeVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
@lombok.NoArgsConstructor                                                       // <.>
public class JodaLocalTimeVm
        implements HasAsciiDocDescription, JodaLocalTimeHolder2 {

//end::class[]
    public JodaLocalTimeVm(final LocalTime initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "LocalTime view model: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @XmlJavaTypeAdapter(org.apache.causeway.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalTimeToStringAdapter.class)
    @XmlElement(required = true)                                                // <.>
    @Getter @Setter
    private LocalTime readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    @XmlJavaTypeAdapter(org.apache.causeway.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalTimeToStringAdapter.class)
    @XmlElement(required = true)
    @Getter @Setter
    private LocalTime readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @XmlJavaTypeAdapter(org.apache.causeway.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalTimeToStringAdapter.class)
    @Getter @Setter
    private LocalTime readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    @XmlJavaTypeAdapter(org.apache.causeway.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalTimeToStringAdapter.class)
    @Getter @Setter
    private LocalTime readWriteOptionalProperty;

}
//end::class[]
