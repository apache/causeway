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
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("university.calc.EditableMementoViewModel")
@DomainObject(nature = Nature.VIEW_MODEL, editing = Editing.ENABLED)
@XmlRootElement(name = "editableMementoViewModel")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class EditableMementoViewModel extends EditableMementoViewModelContract {

    public EditableMementoViewModel(final int age) {
        this.age = age;
    }

    @ObjectSupport
    public String title() {
        return "Age " + age;
    }

    @Property
    @XmlElement(required = true)
    @Getter @Setter
    private int age;

    public String validateAge(final int proposedAge) {
        return proposedAge < 18
                ? "Age must be at least 18"
                : null;
    }
}
