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
package demoapp.dom.domain.actions.progmodel.depargs;

import javax.inject.Named;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.DependentArgsDemoItem")
@Named("depargs.DemoItem")
@NoArgsConstructor
@AllArgsConstructor(staticName="of")
@EqualsAndHashCode // required for the Dependent Arguments demo to work properly
public class DemoItem {

    public String title() {
        return String.format("DemoItem '%s' (%s)", getName(), getParity());
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(describedAs="The name of this 'DemoItem'.")
    @Getter @Setter private String name;

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(describedAs="The parity of this 'DemoItem'.")
    @Getter @Setter private Parity parity;

}
