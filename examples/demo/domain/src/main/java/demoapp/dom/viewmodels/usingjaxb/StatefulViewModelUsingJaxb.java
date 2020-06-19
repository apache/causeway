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
package demoapp.dom.viewmodels.usingjaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "demoapp.StatefulViewModelUsingJaxb")
@XmlType(
)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL
        , objectType = "demoapp.StatefulViewModelUsingJaxb"
)
public class StatefulViewModelUsingJaxb implements HasAsciiDocDescription {

    public String title() {
        return String.format("%s; %s children", getMessage(), getChildren().size());
    }

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    @XmlElement
    private String message;

    @XmlRootElement(name = "child")
    @XmlType
    @XmlAccessorType(XmlAccessType.FIELD)
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

    @Action(associateWith = "children", associateWithSequence = "1", semantics = SemanticsOf.NON_IDEMPOTENT)
    public StatefulViewModelUsingJaxb addChild(final String name) {
        val child = new Child();
        child.setName(name);
        children.add(child);
        return this;
    }

    @Action(associateWith = "children", associateWithSequence = "2", semantics = SemanticsOf.IDEMPOTENT)
    public StatefulViewModelUsingJaxb removeChild(final Child child) {
        children.remove(child);
        return this;
    }
    public List<Child> choices0RemoveChild() { return getChildren(); }
    public String disableRemoveChild() {
        return choices0RemoveChild().isEmpty()? "No children to remove" : null;
    }

}
