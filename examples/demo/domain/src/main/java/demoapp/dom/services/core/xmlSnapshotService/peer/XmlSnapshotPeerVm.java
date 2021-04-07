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
package demoapp.dom.services.core.xmlSnapshotService.peer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.services.core.xmlSnapshotService.peer.child.XmlSnapshotPeerChildVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

//tag::class[]
@XmlRootElement(name = "peer")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = "demo.XmlSnapshotPeerVm"
)
@NoArgsConstructor
public class XmlSnapshotPeerVm implements HasAsciiDocDescription {

    public XmlSnapshotPeerVm(String value) {
        setValue(value);
    }

    @Title
    @Property()
    @PropertyLayout(group = "annotation", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String value;

    @Collection()
    @CollectionLayout()
    @Getter
    private List<XmlSnapshotPeerChildVm> children = new ArrayList<>();

    public XmlSnapshotPeerVm addChild(final String value) {
        val childVm = new XmlSnapshotPeerChildVm(value);
        getChildren().add(childVm);
        return this;
    }

}
//end::class[]
