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
package demoapp.dom.PropertyLayout.named;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyLayoutNamedVm",
        editing = Editing.ENABLED
)
public class PropertyLayoutNamedVm implements HasAsciiDocDescription {

    public String title() {
        return "PropertyLayout#named";
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(named = "Named using annotation", describedAs = "@PropertyLayout(named= \"Named using annotation\")", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "named", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String namedUsingAnnotation;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(describedAs = "<cpt:property id=\"...\"><cpt:named>...</cpt:named></cpt:property>", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "named", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String namedUsingLayout;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(named = "Named <b>uses</b> <i>markup</i>", namedEscaped = false, describedAs = "@PropertyLayout(named= \"Named <b>uses</b> <i>markup</i>\" namedEscaped=false)", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "markup", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String nameUsesMarkup;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(named = "Named <b>but</b> <i>escaped</i>", namedEscaped = false, describedAs = "@PropertyLayout(named= \"Named <b>but</b> <i>unescaped</i>\" namedEscaped=true)", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "markup", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String nameUsesEscapedMarkup;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayoutNamedMetaAnnotation
    @PropertyLayout(describedAs = "@PropertyLayoutNamedMetaAnnotation", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String namedUsingMetaAnnotation;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayoutNamedMetaAnnotation
    @PropertyLayout(named = "Name overrides meta-annotation", describedAs = "meta-annotation overridden using @PropertyLayout(...)", hidden = Where.ALL_TABLES)
    @MemberOrder(name = "meta-annotated", sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String namedUsingMetaAnnotationButOverridden;

}
//end::class[]
