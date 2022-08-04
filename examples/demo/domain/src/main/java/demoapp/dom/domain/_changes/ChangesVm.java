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
package demoapp.dom.domain._changes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.ValueSemantics;
import org.apache.isis.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.jaxb.JavaUtilJaxbAdapters;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.common.v2.OidsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.domain.actions.Action.associateWith.child.ActionAssociateWithChildVm;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.ChangesVm")
@DomainObject(
    nature=Nature.VIEW_MODEL
)
@NoArgsConstructor
public class ChangesVm {

    public String title() {
        int numCreated = changesDto.getObjects().getCreated().getOid().size();
        int numUpdated = changesDto.getObjects().getUpdated().getOid().size();
        int numDeleted = changesDto.getObjects().getDeleted().getOid().size();
        LocalDateTime completedAt = JavaTimeXMLGregorianCalendarMarshalling.toLocalDateTime(changesDto.getCompletedAt());
        return String.format("%s: %d created, %d updated, %d deleted", completedAt, numCreated, numUpdated, numDeleted);
    }

    @ValueSemantics(provider = "pretty-render")
    @Getter @Setter
    private ChangesDto changesDto;

    public ChangesVm(ChangesDto dto) {
        this.changesDto = dto;
    }
}
