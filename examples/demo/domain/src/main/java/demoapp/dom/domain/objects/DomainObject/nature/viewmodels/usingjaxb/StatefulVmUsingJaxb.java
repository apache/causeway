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
package demoapp.dom.domain.objects.DomainObject.nature.viewmodels.usingjaxb;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Title;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

//tag::class[]
@XmlRootElement(name = "root")                              // <.>
@XmlType                                                    // <.>
@XmlAccessorType(XmlAccessType.FIELD)                       // <.>
@Named("demo.StatefulVmUsingJaxb")
@DomainObject(
        nature=Nature.VIEW_MODEL)
public class StatefulVmUsingJaxb implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return String.format("%s; %s children", getMessage(), getChildren().size());
    }

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    @XmlElement
    private String message;                                 // <.>

//end::class[]
//tag::child[]
    @XmlRootElement
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
    @Named("demo.StatefulViewModelUsingJaxb.Child")
    @DomainObject(
            nature=Nature.VIEW_MODEL)
    @Data
    public static class Child {
        @Title
        private String name;
    }

    @Getter @Setter
    @Collection
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    private List<Child> children = new ArrayList<>();
//end::child[]

//tag::addChild[]
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(sequence = "1")
    public StatefulVmUsingJaxb addChild(final String name) {
        val child = new Child();
        child.setName(name);
        children.add(child);
        return this;
    }

    @Action(choicesFrom = "children", semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(sequence = "2")
    public StatefulVmUsingJaxb removeChild(final Child child) {
        children.remove(child);
        return this;
    }
    @MemberSupport public List<Child> choices0RemoveChild() { return getChildren(); }
    @MemberSupport public String disableRemoveChild() {
        return choices0RemoveChild().isEmpty()? "No children to remove" : null;
    }
//end::addChild[]

    //tag::class[]
}
//end::class[]
