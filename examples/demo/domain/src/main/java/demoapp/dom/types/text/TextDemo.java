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
package demoapp.dom.types.text;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.value.Password;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType="demo.Text", editing=Editing.ENABLED)
@ToString
@Log4j2
public class TextDemo implements HasAsciiDocDescription {


    // -- EDITABLE

    @Property
    @XmlElement @Getter @Setter private String string;

    @Property
    @PropertyLayout(multiLine=3)
    @XmlElement @Getter @Setter private String stringMultiline;

    // -- READONLY

    @Property(editing=Editing.DISABLED)
    @XmlElement @Getter @Setter private String stringReadonly;

    @Property(editing=Editing.DISABLED)
    @PropertyLayout(multiLine=3)
    @XmlElement @Getter @Setter private String stringMultilineReadonly;

    // -- MASKED
    
    @Property(optionality = Optionality.OPTIONAL)
    @XmlElement @Getter @Setter private Password secret;
    
    
}
