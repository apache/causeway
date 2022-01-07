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
package demoapp.dom.domain.actions.Action.typeOf.child;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        logicalTypeName = "demo.ActionTypeOfChildVm"
)
@NoArgsConstructor
public class ActionTypeOfChildVm implements HasAsciiDocDescription {

    public ActionTypeOfChildVm(final String value) {
        setValue(value);
        setValueUpper(value.toUpperCase());
        setValueLower(value.toLowerCase());
        setValueReversed(reverse(value));
    }

    @ObjectSupport public String title() {
        return getValue();
    }

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String value;

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String valueUpper;

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private String valueLower;

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "4")
    @XmlElement(required = false)
    @Getter @Setter
    private String valueReversed;

    private static String reverse(final String value) {
        return new StringBuilder(value).reverse().toString();
    }


}
//end::class[]
