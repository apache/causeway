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
package demoapp.dom.domain.objects.DomainObject.xxxDomainEvent;

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
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.objects.DomainObject.xxxDomainEvent.child.DomainObjectXxxDomainEventChildVm;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DomainObjectxxxDomainEventPage")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(
    actionDomainEvent = DomainObjectXxxDomainEventPage.ActionEvent.class,           // <.>
    propertyDomainEvent = DomainObjectXxxDomainEventPage.PropertyEvent.class,       // <.>
    collectionDomainEvent = DomainObjectXxxDomainEventPage.CollectionEvent.class,   // <.>
    nature=Nature.VIEW_MODEL
)
@DomainObjectLayout(cssClassFa="fa-asterisk")
public class DomainObjectXxxDomainEventPage implements HasAsciiDocDescription {

    public interface DomainObjectXxxDomainEventMarker {}                            // <.>

    public static class ActionEvent                                                 // <1>
            extends ActionDomainEvent<DomainObjectXxxDomainEventPage>
            implements DomainObjectXxxDomainEventMarker {}

    public static class PropertyEvent                                               // <2>
            extends PropertyDomainEvent<DomainObjectXxxDomainEventPage, Object>
            implements DomainObjectXxxDomainEventMarker {}

    public static class CollectionEvent                                             // <3>
            extends CollectionDomainEvent<DomainObjectXxxDomainEventPage, Object>
            implements DomainObjectXxxDomainEventMarker {}
    // ...

//end::class[]
    public DomainObjectXxxDomainEventPage(final String text) {
        this.text = text;
    }

    @ObjectSupport public String title() {
        return "@DomainObject#xxxDomainEvent";
    }

    public void addChild(String value) {
        this.getChildren().add(new DomainObjectXxxDomainEventChildVm(value));
    }

//tag::class[]
    @Property(editing = Editing.ENABLED)                                            // <.>
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Action(semantics = SemanticsOf.SAFE)
    public DomainObjectXxxDomainEventPage updateTextDirectly(String text) {         // <.>
        setText(text);
        return this;
    }
    // ...

//end::class[]
    @MemberSupport public String default0UpdateTextDirectly() {
        return getText();
    }

//tag::class[]
    @Collection
    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    @Getter @Setter
    private List<DomainObjectXxxDomainEventChildVm> children = new ArrayList<>();
}
//end::class[]
