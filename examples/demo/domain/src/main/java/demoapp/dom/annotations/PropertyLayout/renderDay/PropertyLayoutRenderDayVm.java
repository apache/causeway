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
package demoapp.dom.annotations.PropertyLayout.renderDay;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderDay;
import org.apache.isis.applib.util.JaxbAdapters;
import org.apache.isis.schema.jaxbadapters.JodaLocalDateStringAdapter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyLayoutRenderDayVm",
        editing = Editing.ENABLED
)
@NoArgsConstructor
public class PropertyLayoutRenderDayVm implements HasAsciiDocDescription {

    public PropertyLayoutRenderDayVm(LocalDate localDate) {
        startDateInclusive = localDate;
        endDateExclusive = startDateInclusive.plusDays(7);
    }

    public String title() {
        return "PropertyLayout#renderDay";
    }

//tag::render-not-specified[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        renderDay = RenderDay.NOT_SPECIFIED         // <.>
        , describedAs =
            "@PropertyLayout(renderDay = NOT_SPECIFIED)"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JodaLocalDateStringAdapter.ForJaxb.class)
    @Getter @Setter
    private LocalDate startDateInclusive;
//end::render-not-specified[]

//tag::render-as-day-before[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        renderDay = RenderDay.AS_DAY_BEFORE         // <.>
        , describedAs =
            "@PropertyLayout(renderDay = AS_DAY_BEFORE)"
    )
    @MemberOrder(name = "annotation", sequence = "2")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JodaLocalDateStringAdapter.ForJaxb.class)
    @Getter @Setter
    private LocalDate endDateExclusive;
//end::render-as-day-before[]

//tag::render-as-day[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        renderDay = RenderDay.AS_DAY             // <.>
        , describedAs =
            "Value of getEndDateExclusive(), but @PropertyLayout(renderDay = AS_DAY)"
    )
    @MemberOrder(name = "annotation", sequence = "3")
    public LocalDate getEndDateExclusiveRaw() {
        return getEndDateExclusive();
    }
//end::render-as-day[]
}
//end::class[]
