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
package demoapp.dom.types.javalang.voids;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.resources.AsciiDocReaderService;

@XmlRootElement(name = "demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.JavaLangVoids")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
//@Log4j2
public class JavaLangVoids implements HasAsciiDocDescription {


    @ObjectSupport public String title() {
        return "Void data type";
    }

    @Property
    @PropertyLayout(labelPosition= LabelPosition.NONE)
    public AsciiDoc getSectionDescription() {
        return asciiDocReaderService.readFor(this, "sectionDescription");
    }

//tag::returnsVoid[]
    @Action(semantics = SemanticsOf.SAFE)
    public void returnsVoid() {}
//end::returnsVoid[]

//tag::returnsJavaLangVoid[]
    @Action(semantics = SemanticsOf.SAFE)
    public Void returnsJavaLangVoid() {
        return null;
    }
//end::returnsJavaLangVoid[]

    @Inject @XmlTransient AsciiDocReaderService asciiDocReaderService;

}
