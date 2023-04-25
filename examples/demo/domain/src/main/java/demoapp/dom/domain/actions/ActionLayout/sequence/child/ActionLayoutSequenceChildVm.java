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
package demoapp.dom.domain.actions.ActionLayout.sequence.child;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.*;

import org.apache.causeway.applib.annotation.*;

//tag::class[]
@XmlRootElement(name = "child")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.ActionLayoutSequenceChildVm")
@DomainObject(nature=Nature.VIEW_MODEL)
@NoArgsConstructor
public class ActionLayoutSequenceChildVm implements HasAsciiDocDescription {

    public ActionLayoutSequenceChildVm(final String value) {
        setValue(value);
    }

    @ObjectSupport public String title() {
        return getValue();
    }

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String value;



}
//end::class[]
