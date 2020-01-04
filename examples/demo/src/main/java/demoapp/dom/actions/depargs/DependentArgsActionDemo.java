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
package demoapp.dom.actions.depargs;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.value.Markup;

import lombok.Getter;

import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.DependentArgs", editing=Editing.ENABLED)
public class DependentArgsActionDemo extends DemoStub {

    // -- INIT

    @Getter private final Set<DemoItem> items = new LinkedHashSet<>();

    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getText() {
        return new Markup("Click one of these 5 actions to see how dependent arguments work.");
    }

    @Override
    public void initDefaults() {
        items.clear();
        items.add(DemoItem.of("first", Parity.ODD));
        items.add(DemoItem.of("second", Parity.EVEN));
        items.add(DemoItem.of("third", Parity.ODD));
        items.add(DemoItem.of("last", Parity.EVEN));
    }
    
    // -- DEBUG
    
//    @ActionLayout(named="Choices", promptStyle = PromptStyle.DIALOG_MODAL)
//    @Action(semantics = SemanticsOf.SAFE)
//    public DependentArgsActionDemo useChoices(
//
//            // PARAM 0
//            @Parameter(optionality = Optionality.MANDATORY)
//            Parity parity,
//
//            // PARAM 1
//            @Parameter(optionality = Optionality.MANDATORY)
//            DemoItem item
//
//            ) {
//        
//        return this;
//    }
//
//    // -- PARAM 1 (DemoItem)
//
//    
//    public Collection<DemoItem> choices1useChoices(
//            
//            Parity parity // <-- the refining parameter from the dialog above
//            
//            ) {
//        
//        if(parity == null) {
//            return this.getItems();
//        }
//        return this.getItems()
//                .stream()
//                .filter(item->parity == item.getParity())
//                .collect(Collectors.toList());
//    }

}

