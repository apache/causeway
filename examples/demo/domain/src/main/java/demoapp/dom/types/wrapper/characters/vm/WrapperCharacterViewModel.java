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
package demoapp.dom.types.wrapper.characters.vm;

import javax.jdo.annotations.Column;
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
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.wrapper.characters.holder.WrapperCharacterHolder;

//tag::class[]
@XmlRootElement(name = "demo.WrapperCharacterViewModel")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.WrapperCharacterViewModel"
)
@lombok.NoArgsConstructor                                                       // <.>
public class WrapperCharacterViewModel
        implements HasAsciiDocDescription, WrapperCharacterHolder {

//end::class[]
    public WrapperCharacterViewModel(Character initialValue) {
        this.readOnlyProperty = initialValue;
        this.readOnlyProperty2 = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "Character (wrapper) view model: ")
    @XmlElement(required = true)
    @Getter @Setter
    private Character readOnlyProperty;                                         // <.>

    @Property
    @PropertyLayout(hidden = Where.ALL_TABLES)
    @XmlElement(required = true)
    @Getter @Setter
    private Character readOnlyProperty2;

    @Property(editing = Editing.ENABLED)
    @XmlElement(required = true)                                                // <.>
    @Getter @Setter
    private Character readWriteProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)    // <.>
    @Getter @Setter
    private Character readWriteOptionalProperty;

}
//end::class[]
