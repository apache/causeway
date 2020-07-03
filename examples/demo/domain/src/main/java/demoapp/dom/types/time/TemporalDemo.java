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
package demoapp.dom.types.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.util.JaxbAdapters.DateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.LocalDateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.LocalDateTimeAdapter;
import org.apache.isis.applib.util.JaxbAdapters.OffsetDateTimeAdapter;
import org.apache.isis.applib.util.JaxbAdapters.SqlDateAdapter;
import org.apache.isis.applib.util.JaxbAdapters.SqlTimestampAdapter;
import org.apache.isis.core.commons.internal.debug._Probe;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.Temporal", editing=Editing.ENABLED)
@Log4j2
public class TemporalDemo implements HasAsciiDocDescription {

    public String title() {
        return "Temporal Demo";
    }

    // -- DATE ONLY (LOCAL TIME)


    @Property
    @PropertyLayout(describedAs="java.time.LocalDate")
    @XmlElement @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @Getter //@Setter 
    private LocalDate javaLocalDate;
    public void setJavaLocalDate(LocalDate javaLocalDate) {
        this.javaLocalDate = javaLocalDate;
        _Probe.sysOut("setJavaLocalDate %s", javaLocalDate);
    }
    @Model
    public String validateJavaLocalDate(LocalDate javaLocalDate) {
        if(javaLocalDate.isBefore(LocalDate.now())) {
            return "cannot be in the past";
        }
        return null;
    }

    // -- DATE AND TIME (LOCAL TIME)

    @Property
    @PropertyLayout(describedAs="java.util.Date")
    @XmlElement @XmlJavaTypeAdapter(DateAdapter.class)
    @Getter @Setter private Date javaUtilDate;

    @Property
    @PropertyLayout(describedAs="java.sql.Timestamp")
    @XmlElement @XmlJavaTypeAdapter(SqlTimestampAdapter.class)
    @Getter @Setter private java.sql.Timestamp javaSqlTimestamp;

    @Property
    @PropertyLayout(describedAs="java.time.LocalDateTime")
    @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @Getter @Setter private LocalDateTime javaLocalDateTime;

    // -- DATE AND TIME (WITH TIMEZONE OFFSET)

    @Property
    @PropertyLayout(describedAs="java.time.OffsetDateTime")
    @XmlElement @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
    @Getter @Setter private OffsetDateTime javaOffsetDateTime;

    // --



}
