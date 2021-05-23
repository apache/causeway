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
package demoapp.dom.types.javaawt.images.vm;

import java.awt.image.BufferedImage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.JavaAwtBufferedImageVm"
)
@lombok.NoArgsConstructor                                               // <.>
public class JavaAwtBufferedImageVm
        implements HasAsciiDocDescription
//end::class[]
// label positions not yet supported
//        , JavaAwtBufferedImageHolder2
//tag::class[]
{

//end::class[]
    public JavaAwtBufferedImageVm(BufferedImage initialValue) {
        this.readOnlyProperty = initialValue;
//        this.readWriteProperty = initialValue;    // editable properties not yet supported
    }

    // @Title not yet supported
    public String title() {
        return "Image view model";
    }

//    @Title(prepend = "Image view model: ")  // not yet supported
//tag::class[]
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    @XmlElement(required = true)                                        // <.>
    @Getter @Setter
    private BufferedImage readOnlyProperty;

//end::class[]
// editable properties not yet supported:
//    @Property(editing = Editing.ENABLED)                                // <.>
//    @PropertyLayout(group = "editable-properties", sequence = "1")
//    @XmlElement(required = true)
//    @Getter @Setter
//    private BufferedImage readWriteProperty;

//tag::class[]
    @Property(optionality = Optionality.OPTIONAL)                       // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    @Getter @Setter
    private BufferedImage readOnlyOptionalProperty;

//end::class[]
// editable properties not yet supported:
//    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
//    @PropertyLayout(group = "optional-properties", sequence = "2")
//    @Getter @Setter
//    private BufferedImage readWriteOptionalProperty;

//tag::class[]
}
//end::class[]
