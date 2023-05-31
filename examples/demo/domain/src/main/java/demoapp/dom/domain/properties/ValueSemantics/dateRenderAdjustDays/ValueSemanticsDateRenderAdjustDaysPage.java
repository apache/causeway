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
package demoapp.dom.domain.properties.ValueSemantics.dateRenderAdjustDays;

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
@Named("demo.ValueSemanticsDateRenderAdjustDaysPage")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED
)
@NoArgsConstructor
public class ValueSemanticsDateRenderAdjustDaysPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "@ValueSemantics#dateRenderAdjustDays";
    }

//tag::render-not-specified[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(describedAs = "start date of the date range")
    @ValueSemantics
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate startDate;
//end::render-not-specified[]

//tag::render-as-day-before[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(describedAs = "end date of the date range (up to and including)")
    @ValueSemantics(
            dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE         // <.>
    )
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
    @Getter @Setter
    private LocalDate endDate;
//end::render-as-day-before[]

//tag::render-as-day[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(describedAs = "end date of the date range (excluding)")
    @ValueSemantics(dateRenderAdjustDays = 0)                           // <.>
    public LocalDate getEndDateNotAdjusted() {
        return getEndDate();
    }
//end::render-as-day[]

}
//end::class[]
