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
package demoapp.dom.featured.layout.tabs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.Tab")
public class TabDemo implements HasAsciiDocDescription {

    public String title() {
        return "Tab Demo";
    }

    // -- HIDE

    @Action
    public TabDemo doHideField(){
        hidden = true;
        return this;
    }

    @Action
    public TabDemo doShowField(){
        hidden = false;
        return this;
    }

    // -- DEMO FIELD 1

    @Getter @Setter private String field1 = "field 1";

    public boolean hideField1() {
        return hidden;
    }

    // -- DEMO FIELD 2-4

    @Getter @Setter private String field2 = "field 2";

    @Property(editing = Editing.ENABLED)
    @Getter @Setter private String field3 = "field 3";

    @Property(editing = Editing.ENABLED)
    @Getter @Setter private String field4 = "field 4";

    // ---

    private boolean hidden = false;


}
