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

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.causeway.schema.chg.v2.ChangesDto;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.ChangesVm")
@DomainObject(
    nature=Nature.VIEW_MODEL
)
@NoArgsConstructor
@AllArgsConstructor
public class ChangesVm
        implements
        HasAsciiDocDescription {

    @ObjectSupport public String title() {
        int numCreated = changesDto.getObjects().getCreated().getOid().size();
        int numUpdated = changesDto.getObjects().getUpdated().getOid().size();
        int numDeleted = changesDto.getObjects().getDeleted().getOid().size();
        LocalDateTime completedAt = JavaTimeXMLGregorianCalendarMarshalling
                .toLocalDateTime(changesDto.getCompletedAt());
        return String.format("%s: %d created, %d updated, %d deleted",
                completedAt, numCreated, numUpdated, numDeleted);
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(fieldSetName = "ChangesDto", labelPosition = LabelPosition.NONE)
    @ValueSemantics(provider = "pretty-render")
    @Getter @Setter
    private ChangesDto changesDto;

}
