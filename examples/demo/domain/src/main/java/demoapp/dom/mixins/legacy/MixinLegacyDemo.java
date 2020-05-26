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
package demoapp.dom.mixins.legacy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.mixins.DemoItem;
import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.MixinLegacyDemo")
public class MixinLegacyDemo extends DemoStub {

    @Override
    public String title() {
        return "Mixin Demo (Legacy)";
    }

    @Property(editing = Editing.DISABLED) // inline edit disabled, but allows updates via mixin
    @PropertyLayout(multiLine = 3)
    @Getter @Setter private String note;
    
    // ---

    List<DemoItem> collection;
    
    @Override @Programmatic
    public void initDefaults() {

        note = "Update me! The button below is contributed by one of my mixins.";
        
        collection = new ArrayList<>();
        collection.add(DemoItem.of("first", null));
        collection.add(DemoItem.of("second", collection.get(0)));
        collection.add(DemoItem.of("third", collection.get(1)));

    }

}
