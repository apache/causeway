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
package demoapp.dom.annotDomain.Property.fileAccept;

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
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.extensions.viewer.wicket.pdfjs.applib.annotations.PdfJsViewer;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.PropertyFileAcceptVm",
        editing = Editing.ENABLED
)
public class PropertyFileAcceptVm implements HasAsciiDocDescription {

    public String title() {
        return "Property#fileAccept";
    }

//tag::annotation[]
    @Property(
        fileAccept = ".pdf"                                 // <.>
    )
    @PropertyLayout(
        describedAs =
            "@Property(fileAccept = \".pdf\")"
    )
    @MemberOrder(name = "annotation", sequence = "1")
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
            "@Property(fileAccept = \".txt\")"
    )
    @MemberOrder(name = "annotation", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Clob txtPropertyUsingAnnotation;
//end::annotation-clob[]

//tag::meta-annotated[]
    @Property()
    @FileAcceptPdfMetaAnnotation                            // <.>
    @PropertyLayout(
        describedAs = "@FileAcceptPdfMetaAnnotation"
    )
    @MemberOrder(name = "meta-annotated", sequence = "1")
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
            "@FileAcceptPdfMetaAnnotation @PropertyLayout(...)"
    )
    @MemberOrder(name = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private Blob docxPropertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
