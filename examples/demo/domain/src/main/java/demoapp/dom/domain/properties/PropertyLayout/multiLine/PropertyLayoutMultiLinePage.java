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
package demoapp.dom.domain.properties.PropertyLayout.multiLine;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.PropertyLayoutMultiLineVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED)
public class PropertyLayoutMultiLinePage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "@PropertyLayout#multiLine";
    }

//tag::annotation[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        multiLine = 5                           // <.>
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotation;
//end::annotation[]

//tag::annotation-readonly[]
    @Property(
        optionality = Optionality.OPTIONAL,
        editing = Editing.DISABLED              // <.>
    )
    @PropertyLayout(
        multiLine = 5
    )
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingAnnotationReadOnly;
//end::annotation-readonly[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout()
    @XmlElement(required = false)
    @Getter @Setter
    private String propertyUsingLayout;
//end::layout-file[]

}
//end::class[]
