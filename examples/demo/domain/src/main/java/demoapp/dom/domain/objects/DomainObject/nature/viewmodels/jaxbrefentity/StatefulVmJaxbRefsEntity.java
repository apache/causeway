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
package demoapp.dom.domain.objects.DomainObject.nature.viewmodels.jaxbrefentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.jaxb.PersistentEntitiesAdapter;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType(
        propOrder = {"message", "favoriteChild", "children"}
)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL
        , logicalTypeName = "demo.StatefulViewModelJaxbRefsEntity"
)
public class StatefulVmJaxbRefsEntity implements HasAsciiDocDescription {

    @XmlTransient @Inject
    private ValueHolderRepository<String, ? extends JaxbRefEntity> childEntities;

    @ObjectSupport public String title() {
        return String.format("%s; %s children", getMessage(), getChildren().size());
    }

    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    @XmlElement
    private String message;

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @XmlElement(required = false)
    @XmlJavaTypeAdapter(PersistentEntityAdapter.class)
    private JaxbRefEntity favoriteChild = null;

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(associateWith = "favoriteChild", sequence = "1")
    public StatefulVmJaxbRefsEntity changeFavoriteChild(final JaxbRefEntity newFavorite) {
        favoriteChild = newFavorite;
        return this;
    }
    @MemberSupport public List<JaxbRefEntity> choices0ChangeFavoriteChild() {
        List<JaxbRefEntity> children = new ArrayList<>(getChildren());
        children.remove(getFavoriteChild());
        return children;
    }
    @MemberSupport public String disableChangeFavoriteChild() {
        switch (getChildren().size()) {
            case 0: return "no children";
            case 1: return "only child";
            default: return null;
        }
    }

//end::class[]

    //XXX[ISIS-2384] potentially fails with NPE
    @Action(choicesFrom = "children")
    public StatefulVmJaxbRefsEntity suffixSelected(final List<JaxbRefEntity> children) {
        for(JaxbRefEntity child : children) {
            child.setName(child.getName() + ", Jr");
        }
        return this;
    }

    //XXX shortcut for debugging
    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(associateWith = "children", sequence = "2")
    public StatefulVmJaxbRefsEntity addAll() {
        Objects.requireNonNull(childEntities,
                "ViewModel must have its injections points resolved, before any actions can be invoked.");
        val all = childEntities.all();
        getChildren().clear();
        getChildren().addAll(all);
        return this;
    }

    //XXX[ISIS-2383] in support of an editable property ...
    @MemberSupport public List<JaxbRefEntity> choicesFavoriteChild() {
        return choices0ChangeFavoriteChild(); // reuse logic from above
    }
    @MemberSupport public String disableFavoriteChild() {
        return disableChangeFavoriteChild(); // reuse logic from above
    }

//tag::class[]
    @Getter @Setter
    @Collection
    @XmlElement(name = "child")
    @XmlJavaTypeAdapter(PersistentEntitiesAdapter.class)
    private List<JaxbRefEntity> children = new ArrayList<>();

    @Action(choicesFrom = "children", semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(sequence = "1")
    public StatefulVmJaxbRefsEntity addChild(final JaxbRefEntity child) {
        children.add(child);
        if(children.size() == 1) {
            setFavoriteChild(child);
        }
        return this;
    }

    @Action(choicesFrom = "children", semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(sequence = "2")
    public StatefulVmJaxbRefsEntity removeChild(final JaxbRefEntity child) {
        children.remove(child);
        return this;
    }
    @MemberSupport public List<JaxbRefEntity> choices0RemoveChild() { return getChildren(); }
    @MemberSupport public String disableRemoveChild() {
        return choices0RemoveChild().isEmpty()? "No children to remove" : null;
    }

}
//end::class[]
