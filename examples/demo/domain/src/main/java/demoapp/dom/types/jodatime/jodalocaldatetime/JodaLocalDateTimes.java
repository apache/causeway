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
package demoapp.dom.types.jodatime.jodalocaldatetime;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Title;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.Samples;
import demoapp.dom.types.jodatime.jodalocaldatetime.persistence.JodaLocalDateTimeEntity;
import demoapp.dom.types.jodatime.jodalocaldatetime.vm.JodaLocalDateTimeVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.JodaLocalDateTimes", editing=Editing.ENABLED)
//@Log4j2
public class JodaLocalDateTimes implements HasAsciiDocDescription {

    @Title
    public String title() {
        return "org.joda.time.LocalDateTime data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public JodaLocalDateTimeVm openViewModel(final org.joda.time.LocalDateTime initialValue) {
        return new JodaLocalDateTimeVm(initialValue);
    }
    @MemberSupport
    public org.joda.time.LocalDateTime default0OpenViewModel() {
        return samples.single();
    }

    @Collection
    public List<? extends JodaLocalDateTimeEntity> getEntities() {
        return entities!=null
                ? entities.all()
                : Collections.emptyList();
    }

    @Autowired(required = false)
    @XmlTransient
    ValueHolderRepository<org.joda.time.LocalDateTime, ? extends JodaLocalDateTimeEntity> entities;

    @Inject
    @XmlTransient
    Samples<LocalDateTime> samples;

}
