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
package demoapp.dom.domain.properties.PropertyLayout.repainting;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Repainting;
import org.apache.isis.applib.value.Blob;
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
        logicalTypeName = "demo.PropertyLayoutRepaintingVm"
)
public class PropertyLayoutRepaintingVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "PropertyLayout#repainting";
    }

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        describedAs =
            "Editable property " +
            "(PDFs should not repaint if it changes)",
        fieldSetId = "edit", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String editMe;

//tag::annotation[]
    @Property()
    @PropertyLayout(
        repainting = Repainting.NO_REPAINT                  // <.>
        , describedAs =
            "@PropertyLayout(repainting = NO_REPAINT)",
        fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = true)
    @PdfJsViewer                                            // <.>
    @Getter @Setter
    private Blob propertyUsingAnnotation;
//end::annotation[]

//tag::layout-file[]
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(                                        // <.>
        describedAs =
            "<cpt:property id=\"...\" repainting = \"NO_REPAINT\"/>",
        fieldSetId = "layout-file", sequence = "1")
    @XmlElement(required = false)
//    @PdfJsViewer
    @Getter @Setter
    private Blob propertyUsingLayout;
//end::layout-file[]

//tag::meta-annotated[]
    @RepaintingNoRepaintMetaAnnotation                      // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        describedAs = "@RepaintingNoRepaintMetaAnnotation",
        fieldSetId = "meta-annotated", sequence = "1")
    @XmlElement(required = false)
//    @PdfJsViewer
    @Getter @Setter
    private Blob propertyUsingMetaAnnotation;
//end::meta-annotated[]

//tag::meta-annotated-overridden[]
    @RepaintingRepaintMetaAnnotation                      // <.>
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
        repainting = Repainting.NO_REPAINT                // <.>
        , describedAs =
            "@RepaintingRepaintMetaAnnotation @PropertyLayout(...)",
        fieldSetId = "meta-annotated-overridden", sequence = "1")
    @XmlElement(required = false)
//    @PdfJsViewer
    @Getter @Setter
    private Blob propertyUsingMetaAnnotationButOverridden;
//end::meta-annotated-overridden[]

}
//end::class[]
