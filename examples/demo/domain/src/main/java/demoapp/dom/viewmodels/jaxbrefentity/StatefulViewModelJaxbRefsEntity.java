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
package demoapp.dom.viewmodels.jaxbrefentity;

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
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "demoapp.StatefulViewModelJaxbRefsEntity")
@XmlType(
        propOrder = {"message", "favoriteChild", "children"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL
        , objectType = "demoapp.StatefulViewModelJaxbRefsEntity"
)
public class StatefulViewModelJaxbRefsEntity implements HasAsciiDocDescription {

    public String title() {
        return String.format("%s; %s children", getMessage(), getChildren().size());
    }

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    @XmlElement
    private String message;

    @Getter @Setter
    @Property(editing = Editing.DISABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    private ChildJdoEntity favoriteChild = null;

    @Action(semantics = SemanticsOf.IDEMPOTENT, associateWith = "favoriteChild", associateWithSequence = "1")
    public StatefulViewModelJaxbRefsEntity changeFavoriteChild(ChildJdoEntity newFavorite) {
        favoriteChild = newFavorite;
        return this;
    }
    public List<ChildJdoEntity> choices0ChangeFavoriteChild() {
        ArrayList<ChildJdoEntity> children = new ArrayList<>(getChildren());
        children.remove(getFavoriteChild());
        return children;
    }
    public String disableChangeFavoriteChild() {
        switch (getChildren().size()) {
            case 0: return "no children";
            case 1: return "only child";
            default: return null;
        }
    }

    // using a ediatble property seems to fail, though...
//    public List<ChildEntity> choicesFavoriteChild() {
//        return getChildren();
//    }
//    public String disableFavoriteChild() {
//        return getChildren().isEmpty() ? "no children" : null;
//    }

    @Getter @Setter
    @Collection
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    private List<ChildJdoEntity> children = new ArrayList<>();

    @Action(associateWith = "children", associateWithSequence = "1", semantics = SemanticsOf.NON_IDEMPOTENT)
    public StatefulViewModelJaxbRefsEntity addChild(final ChildJdoEntity child) {
        children.add(child);
        if(children.size() == 1) {
            setFavoriteChild(child);
        }
        return this;
    }

    @Action(associateWith = "children", associateWithSequence = "2", semantics = SemanticsOf.IDEMPOTENT)
    public StatefulViewModelJaxbRefsEntity removeChild(final ChildJdoEntity child) {
        children.remove(child);
        return this;
    }
    public List<ChildJdoEntity> choices0RemoveChild() { return getChildren(); }
    public String disableRemoveChild() {
        return choices0RemoveChild().isEmpty()? "No children to remove" : null;
    }

}
