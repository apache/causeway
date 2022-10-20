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
package demoapp.dom.domain.properties.PropertyLayout.renderDay;

import java.time.LocalDate;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.jaxb.JavaTimeJaxbAdapters;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.PropertyLayoutRenderDayVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED)
@NoArgsConstructor
public class PropertyLayoutRenderDayVm implements HasAsciiDocDescription {

    public PropertyLayoutRenderDayVm(final LocalDate localDate) {
        startDate = localDate;
        endDate = startDate.plusDays(7);
    }

    @ObjectSupport public String title() {
        return "PropertyLayout#renderDay";
    }

//tag::render-not-specified[]
    @Property(optionality = Optionality.OPTIONAL)
    @ValueSemantics                                 // <.>
    @PropertyLayout(describedAs = "@ValueSemantics",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate startDate;
//end::render-not-specified[]

//tag::render-as-day-before[]
    @Property(optionality = Optionality.OPTIONAL)
    @ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)                    // <.>
    @PropertyLayout(describedAs =
            "@ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)",
        fieldSetId = "annotation", sequence = "2")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDate;
//end::render-as-day-before[]

//tag::render-as-day[]
    @Property(optionality = Optionality.OPTIONAL)
    @ValueSemantics(dateRenderAdjustDays = 0)                     // <.>
    @PropertyLayout(describedAs =
            "Value of getEndDate(), but @ValueSemantics(dateRenderAdjustDays = 0)",
        fieldSetId = "annotation", sequence = "3")
    public LocalDate getEndDateRaw() {
        return getEndDate();
    }
//end::render-as-day[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                // <.>
        describedAs =
            "<cpt:property id=\"endDateLayoutFile\" " +
            "renderedAsDayBefore=\"true\"/>",
        fieldSetId = "layout-file", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingLayout;
//end::layout-file[]

//tag::meta-annotation[]
    @RenderDayMetaAnnotationEndDateExclusive        // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        describedAs =
            "@RenderDayMetaAnnotationEndDateExclusive",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingMetaAnnotation;
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @RenderDayMetaAnnotationStartDateInclusive      // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)
    @PropertyLayout(describedAs =
            "@RenderDayMetaAnnotationEndDateExclusive",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDateUsingMetaAnnotationButOverridden;
//end::meta-annotation-overridden[]

}
//end::class[]
