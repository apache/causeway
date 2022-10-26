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
package org.apache.causeway.applib.domain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.jaxb.PersistentEntitiesAdapter;

import lombok.Getter;

/**
 * The initial idea of {@link DomainObjectList} was to simplify restful clients.
 * <p>
 * In the original Restful Objects specification, invoking an action that returned
 * a list meant that the RO client needed to handle this collection, which didn't
 * have any identity. That made for special case logic in the client.
 * <p>
 * Instead, if the RO client invokes the action but uses the <i>Accept Header</i> to
 * request an object, then the RO viewer would automatically wrap the returned list
 * in this {@link DomainObjectList} view model.
 * <p>
 * Thus, the RO client then only ever needs to know how to render an object, in all cases.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "list")
@XmlType(
        propOrder = {
                "title",
                "actionOwningFqcn",
                "actionId",
                "actionArguments",
                "elementTypeFqcn",
                "objects"
        }
        )
@XmlAccessorType(XmlAccessType.FIELD)
@Named(DomainObjectList.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        nature = Nature.VIEW_MODEL)
@DomainObjectLayout(
        titleUiEvent = DomainObjectList.TitleUiEvent.class,
        iconUiEvent = DomainObjectList.IconUiEvent.class,
        cssClassUiEvent = DomainObjectList.CssClassUiEvent.class,
        layoutUiEvent = DomainObjectList.LayoutUiEvent.class)
public class DomainObjectList {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".DomainObjectList";

    // -- ui event classes
    public static class TitleUiEvent extends CausewayModuleApplib.TitleUiEvent<DomainObjectList>{  }
    public static class IconUiEvent extends CausewayModuleApplib.IconUiEvent<DomainObjectList>{  }
    public static class CssClassUiEvent extends CausewayModuleApplib.CssClassUiEvent<DomainObjectList>{  }
    public static class LayoutUiEvent extends CausewayModuleApplib.LayoutUiEvent<DomainObjectList>{  }


    // -- domain event classes
    public static abstract class PropertyDomainEvent<T> extends CausewayModuleApplib.PropertyDomainEvent<DomainObjectList, T> {  }
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleApplib.CollectionDomainEvent<DomainObjectList, T> {  }
    public static abstract class ActionDomainEvent extends CausewayModuleApplib.ActionDomainEvent<DomainObjectList> {  }


    // -- constructors
    public DomainObjectList() {
    }
    public DomainObjectList(
            final String title,
            final String elementTypeFqcn,
            final String actionOwningFqcn,
            final String actionId,
            final String actionArguments) {
        this.title = title;
        this.elementTypeFqcn = elementTypeFqcn;
        this.actionOwningFqcn = actionOwningFqcn;
        this.actionId = actionId;
        this.actionArguments = actionArguments;
    }


    // -- title
    @Getter
    private String title;
    @ObjectSupport public String title() {
        return title;
    }


    // -- property: elementObjectType
    public static class ElementObjectTypeDomainEvent extends PropertyDomainEvent<String> {  }

    @Property(
            domainEvent = ElementObjectTypeDomainEvent.class,
            editing = Editing.DISABLED
            )
    @Getter
    private String elementTypeFqcn;


    // -- property: actionOwningType
    public static class ActionOwningTypeDomainEvent extends PropertyDomainEvent<String> {  }



    @Property(
            domainEvent = ActionOwningTypeDomainEvent.class,
            optionality = Optionality.OPTIONAL,
            editing = Editing.DISABLED
            )
    @Getter
    private String actionOwningFqcn;

    // -- property: actionId
    public static class ActionIdDomainEvent extends PropertyDomainEvent<String> {  }

    private String actionId;

    @Property(
            domainEvent = ActionIdDomainEvent.class,
            optionality = Optionality.OPTIONAL,
            editing = Editing.DISABLED
            )
    public String getActionId() {
        return actionId;
    }


    // -- property: actionArguments
    public static class ActionArgumentsDomainEvent extends PropertyDomainEvent<String> {  }

    private String actionArguments;

    @Property(
            domainEvent = ActionArgumentsDomainEvent.class,
            optionality = Optionality.OPTIONAL,
            editing = Editing.DISABLED
            )
    public String getActionArguments() {
        return actionArguments;
    }


    // -- collection: objects
    public static class ObjectsDomainEvent extends CollectionDomainEvent<Object> {  }

    @XmlJavaTypeAdapter(PersistentEntitiesAdapter.class)
    private List<Object> objects = new ArrayList<>();

    @Collection(
            domainEvent = ObjectsDomainEvent.class
            )
    public List<Object> getObjects() {
        return objects;
    }

    public void setObjects(final List<Object> objects) {
        this.objects = objects;
    }


}
