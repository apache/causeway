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
package demoapp.dom.actions.assoc;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;

import lombok.Getter;

import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.AssociatedAction", editing=Editing.ENABLED)
public class AssociatedActionDemo extends DemoStub {

    @XmlTransient
    @Inject MessageService messageService;

    @Getter private final Set<DemoItem> items = new LinkedHashSet<>();

    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    @Action(associateWith="items")
    public AssociatedActionDemo doSomethingWithItems(Set<DemoItem> items) {
        if(items!=null) {
            items.forEach(item->messageService.informUser(item.getName()));    
        }
        return this;
    }

    @Override
    public void initDefaults() {
        items.clear();
        items.add(DemoItem.of("first"));
        items.add(DemoItem.of("second"));
        items.add(DemoItem.of("third"));
        items.add(DemoItem.of("last"));
    }
    
}
