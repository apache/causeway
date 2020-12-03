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
package demoapp.dom.annotLayout.PropertyLayout.renderDay;

import java.time.LocalDate;

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
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.RenderDay;
import org.apache.isis.applib.jaxb.JodaTimeJaxbAdapters;

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
        startDate = localDate;
        endDate = startDate.plusDays(7);
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
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate startDate;
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
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDate;
//end::render-as-day-before[]

//tag::render-as-day[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        renderDay = RenderDay.AS_DAY             // <.>
        , describedAs =
            "Value of getEndDate(), but @PropertyLayout(renderDay = AS_DAY)"
    )
    @MemberOrder(name = "annotation", sequence = "3")
    public LocalDate getEndDateRaw() {
        return getEndDate();
    }
//end::render-as-day[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                // <.>
        describedAs =
            "<cpt:property id=\"endDateLayoutFile\" " +
            "renderedAsDayBefore=\"true\"/>"
    )
    @MemberOrder(name = "layout-file", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingLayout;
//end::layout-file[]

//tag::meta-annotation[]
    @RenderDayMetaAnnotationEndDateExclusive        // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        describedAs =
            "@RenderDayMetaAnnotationEndDateExclusive"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingMetaAnnotation;
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @RenderDayMetaAnnotationStartDateInclusive      // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        renderDay = RenderDay.AS_DAY_BEFORE
        , describedAs =
            "@RenderDayMetaAnnotationEndDateExclusive"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JodaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingMetaAnnotationButOverridden;
//end::meta-annotation-overridden[]

}
//end::class[]
