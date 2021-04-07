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
package demoapp.dom.services.core.xmlSnapshotService;

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
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.services.core.xmlSnapshotService.child.XmlSnapshotChildVm;
import demoapp.dom.services.core.xmlSnapshotService.peer.XmlSnapshotPeerVm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
    nature=Nature.VIEW_MODEL,
    objectType = "demo.XmlSnapshotParentVm"
)
@NoArgsConstructor
public class XmlSnapshotParentVm implements HasAsciiDocDescription {

    public XmlSnapshotParentVm(String text) {
        this.text = text;
    }

    public String title() {
        return "XmlSnapshotService parent VM";
    }

    @Property(editing = Editing.ENABLED)
    @PropertyLayout(group = "properties", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String text;

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(group = "properties", sequence = "3")
    @XmlElement(required = false)
    @Getter @Setter
    private XmlSnapshotPeerVm peer;

    @Collection()
    @CollectionLayout()
    @Getter
    private List<XmlSnapshotChildVm> children = new ArrayList<>();

    public XmlSnapshotParentVm addChild(final String value) {
        val childVm = new XmlSnapshotChildVm(value);
        getChildren().add(childVm);
        return this;
    }

}
