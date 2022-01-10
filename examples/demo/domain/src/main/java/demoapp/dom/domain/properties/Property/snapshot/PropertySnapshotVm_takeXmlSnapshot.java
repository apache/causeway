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
package demoapp.dom.domain.properties.Property.snapshot;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.xml.XmlService;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.applib.value.Clob;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class PropertySnapshotVm_takeXmlSnapshot {

    @Inject XmlSnapshotService xmlSnapshotService;
    // ...
//end::class[]
    @Inject XmlService xmlService;

    private final PropertySnapshotVm vm;
//tag::class[]
    @MemberSupport public Clob act(final String fileName) {
        val builder = xmlSnapshotService.builderFor(vm);
        val snapshot = builder.build();
        val doc = snapshot.getXmlDocument();
        return asClob(fileName, xmlService.asString(doc));
    }
    // ...
//end::class[]
    @MemberSupport public String default0Act() {
        return "snapshot.xml";
    }

    private static Clob asClob(final String fileName, final String xmlStr) {
        return new Clob(fileName, "application/xml", xmlStr);
    }
//tag::class[]
}
//end::class[]
