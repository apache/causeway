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
package demoapp.dom.types.javatime.javatimelocaldatetime;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.javatime.javatimelocaldatetime.persistence.JavaTimeLocalDateTimeEntity;
import demoapp.dom.types.javatime.javatimelocaldatetime.vm.JavaTimeLocalDateTimeVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.JavaTimeLocalDateTimes", editing=Editing.ENABLED)
//@Log4j2
public class JavaTimeLocalDateTimes implements HasAsciiDocDescription {

    public String title() {
        return "java.time.LocalDateTime data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public JavaTimeLocalDateTimeVm openViewModel(java.time.LocalDateTime initialValue) {
        return new JavaTimeLocalDateTimeVm(initialValue);
    }
    public java.time.LocalDateTime default0OpenViewModel() {
        return java.time.LocalDateTime.of(120, 1, 1, 1, 1);
    }

    @Collection
    public List<? extends JavaTimeLocalDateTimeEntity> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    ValueHolderRepository<java.time.LocalDateTime, ? extends JavaTimeLocalDateTimeEntity> entities;


}
