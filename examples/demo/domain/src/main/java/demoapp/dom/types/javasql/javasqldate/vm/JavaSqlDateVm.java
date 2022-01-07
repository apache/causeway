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
package demoapp.dom.types.javasql.javasqldate.vm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Title;
import org.apache.isis.applib.jaxb.JavaSqlJaxbAdapters;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.javasql.javasqldate.holder.JavaSqlDateHolder3;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        logicalTypeName = "demo.JavaSqlDateVm"
)
@lombok.NoArgsConstructor                                                       // <.>
public class JavaSqlDateVm
        implements HasAsciiDocDescription, JavaSqlDateHolder3 {

//end::class[]
    public JavaSqlDateVm(java.sql.Date initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "java.sql.Date view model: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @XmlElement(required = true)                                                // <.>
    @XmlJavaTypeAdapter(JavaSqlJaxbAdapters.DateToStringAdapter.class)                      // <.>
    @Getter @Setter
    private java.sql.Date readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(JavaSqlJaxbAdapters.DateToStringAdapter.class)
    @Getter @Setter
    private java.sql.Date readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @XmlJavaTypeAdapter(JavaSqlJaxbAdapters.DateToStringAdapter.class)
    @Getter @Setter
    private java.sql.Date readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    @XmlJavaTypeAdapter(JavaSqlJaxbAdapters.DateToStringAdapter.class)
    @Getter @Setter
    private java.sql.Date readWriteOptionalProperty;

}
//end::class[]
