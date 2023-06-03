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
package demoapp.dom.domain.actions.ActionLayout.fieldSet;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("demo.ActionLayoutFieldSetVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-solid fa-vector-square")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class[]
//...
public class ActionLayoutFieldSetPage
//end::class[]
        implements HasAsciiDocDescription
//tag::class[]
{
    @Property()
    @XmlElement
    @Getter @Setter
    private String name;

    @Property()
    @PropertyLayout(multiLine = 5)
    @XmlElement
    @Getter @Setter
    private String notes;

    // ...
//end::class[]

    @ObjectSupport public String title() {
        return "@ActionLayout#fieldSet...";
    }

//tag::fieldsetId[]
    @Action
    @ActionLayout(
        fieldSetId = "id",                      // <.>
        position = ActionLayout.Position.PANEL  // <.>
    )
    public Object updateName(final String newName) {
        // ...
//end::fieldsetId[]
        setName(newName);
        return this;
    }
    public String default0UpdateName() {
        return getName();
//tag::fieldsetId[]
    }
//end::fieldsetId[]

//tag::fieldsetName[]
    @Action
    @ActionLayout(
        fieldSetName = "Detail"                 // <.>
                                                // <.>
    )
    public Object updateNotes(
            @ParameterLayout(multiLine = 5)
            final String newNotes) {
        // ...
//end::fieldsetName[]
        setNotes(newNotes);
        return this;
    }
    public String default0UpdateNotes() {
        return getNotes();
//tag::fieldsetName[]
    }
//end::fieldsetName[]

//tag::class[]
}
//end::class[]
