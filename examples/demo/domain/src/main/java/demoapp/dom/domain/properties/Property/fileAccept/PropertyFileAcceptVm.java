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
package demoapp.dom.domain.properties.Property.fileAccept;

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
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.PropertyFileAcceptVm")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        editing = Editing.ENABLED)
public class PropertyFileAcceptVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Property#fileAccept";
    }

//tag::annotation[]
    @Property(
        fileAccept = ".pdf"                                 // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(fileAccept = \".pdf\")",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Blob pdfPropertyUsingAnnotation;
//end::annotation[]

//tag::annotation-clob[]
    @Property(
        fileAccept = ".txt"                     // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(fileAccept = \".txt\")",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Clob txtPropertyUsingAnnotation;
//end::annotation-clob[]

//tag::meta-annotated[]
    @Property()
    @FileAcceptPdfMetaAnnotation                            // <.>
    @PropertyLayout(
        describedAs = "@FileAcceptPdfMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Blob pdfPropertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @FileAcceptPdfMetaAnnotation                            // <.>
    @Property(
        fileAccept = ".docx"                                // <.>
    )
    @PropertyLayout(
        describedAs =
            "@FileAcceptPdfMetaAnnotation @PropertyLayout(...)",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Blob docxPropertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
