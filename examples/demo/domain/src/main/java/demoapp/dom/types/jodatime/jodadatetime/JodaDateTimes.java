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
package demoapp.dom.types.jodatime.jodadatetime;

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

import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.jodatime.jodadatetime.jdo.JodaDateTimeJdo;
import demoapp.dom.types.jodatime.jodadatetime.jdo.JodaDateTimeJdoEntities;
import demoapp.dom.types.jodatime.jodadatetime.samples.JodaDateTimeSamples;
import demoapp.dom.types.jodatime.jodadatetime.vm.JodaDateTimeVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.JodaDateTimes", editing=Editing.ENABLED)
@Log4j2
public class JodaDateTimes implements HasAsciiDocDescription {

    public String title() {
        return "org.joda.time.DateTime data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public JodaDateTimeVm openViewModel(org.joda.time.DateTime initialValue) {
        return new JodaDateTimeVm(initialValue);
    }
    public org.joda.time.DateTime default0OpenViewModel() {
        return stream.stream().findFirst().orElse(null);
    }

    @Collection
    public List<JodaDateTimeJdo> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    JodaDateTimeJdoEntities entities;

    @Inject
    @XmlTransient
    JodaDateTimeSamples stream;

}
