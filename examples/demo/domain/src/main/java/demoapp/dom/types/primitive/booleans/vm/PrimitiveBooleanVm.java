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
package demoapp.dom.types.primitive.booleans.vm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.primitive.booleans.holder.PrimitiveBooleanHolder2;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature= Nature.VIEW_MODEL,
        objectType = "demo.PrimitiveBooleanVm"
)
@lombok.NoArgsConstructor                                               // <.>
public class PrimitiveBooleanVm
        implements HasAsciiDocDescription, PrimitiveBooleanHolder2 {

//end::class[]
    public PrimitiveBooleanVm(boolean initialValue) {
        this.readOnlyProperty = initialValue;
        this.readWriteProperty = initialValue;
    }

//tag::class[]
    @Title(prepend = "boolean (primitive) view model: ")
    @MemberOrder(name = "read-only-properties", sequence = "1")
    @Getter @Setter
    private boolean readOnlyProperty;                                   // <.>

//end::class[]
    @Property(editing = Editing.ENABLED)
    @PropertyLayout(hidden = Where.EVERYWHERE) // TODO: editable properties broken for view models - new value doesn't stick
    @MemberOrder(name = "editable-properties", sequence = "1")
    @Getter @Setter
    private boolean readWriteProperty;
//tag::class[]

}
//end::class[]
