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
package demoapp.dom.domain.actions.ActionLayout.associateWith;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.ActionLayout.associateWith.child.ActionLayoutAssociateWithChildVm;

@Named("demo.ActionLayoutAssociateWithPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-solid fa-arrows-left-right")
@XmlRootElement(name = "root")
@XmlType()
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
//tag::class[]
// ...
public class ActionLayoutAssociateWithPage
//end::class[]
        implements HasAsciiDocDescription
//tag::class[]
{
    @Property
    @XmlElement
    @Getter @Setter
    private String name;

    @Collection()
    @XmlElement
    @Getter
    private List<ActionLayoutAssociateWithChildVm> children = new ArrayList<>();

    // ...
//end::class[]

    @ObjectSupport public String title() {
        return "@ActionLayout#associateWith";
    }


//tag::associate-with-property[]
    @Action
    @ActionLayout(
        associateWith = "name"  // <.>
    )
    public Object updateName(final String newValue) {
        setName(newValue);
        return this;
    }
    public String default0UpdateName() { return getName(); }

//end::associate-with-property[]

//tag::associate-with-collection[]
    @Action
    @ActionLayout(
            associateWith = "children", // <.>
            sequence = "1"              // <.>
    )
    public Object addChild(final String newValue) {
        // ...
//end::associate-with-collection[]
        getChildren().add(new ActionLayoutAssociateWithChildVm(newValue));
        return this;
//tag::associate-with-collection[]
    }

    @Action
    @ActionLayout(
            associateWith = "children", // <1>
            sequence = "2"              // <2>
    )
    public Object removeChild(final ActionLayoutAssociateWithChildVm child) {
        // ...
//end::associate-with-collection[]
        getChildren().removeIf(x -> x.getValue().equals(child.getValue()));
        return this;
    }
    public List<ActionLayoutAssociateWithChildVm> choices0RemoveChild() {
        return getChildren();
//tag::associate-with-collection[]
    }
//end::associate-with-collection[]

//tag::class[]
}
//end::class[]
