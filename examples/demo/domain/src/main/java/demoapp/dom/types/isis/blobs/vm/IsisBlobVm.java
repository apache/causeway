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
package demoapp.dom.types.isis.blobs.vm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.value.Blob;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.isis.blobs.holder.IsisBlobHolder2;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.IsisBlobVm"
)
@lombok.NoArgsConstructor                                                       // <.>
public class IsisBlobVm
        implements HasAsciiDocDescription, IsisBlobHolder2 {

//end::class[]
    public IsisBlobVm(Blob initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    public String title() {
        return "Blob view model: " +getReadOnlyProperty().getName();
    }

    @MemberOrder(name = "read-only-properties", sequence = "1")
    @XmlJavaTypeAdapter(Blob.JaxbXmlAdapter.class)                         // <.>
    @XmlElement(required = true)                                                // <.>
    @Getter @Setter
    private Blob readOnlyProperty;

    @Property(editing = Editing.ENABLED)                                        // <.>
    @MemberOrder(name = "editable-properties", sequence = "1")
    @XmlJavaTypeAdapter(Blob.JaxbXmlAdapter.class)
    @XmlElement(required = true)
    @Getter @Setter
    private Blob readWriteProperty;

    @Property(optionality = Optionality.OPTIONAL)                               // <.>
    @MemberOrder(name = "optional-properties", sequence = "1")
    @XmlJavaTypeAdapter(Blob.JaxbXmlAdapter.class)
    @Getter @Setter
    private Blob readOnlyOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @MemberOrder(name = "optional-properties", sequence = "2")
    @XmlJavaTypeAdapter(Blob.JaxbXmlAdapter.class)
    @Getter @Setter
    private Blob readWriteOptionalProperty;

}
//end::class[]
