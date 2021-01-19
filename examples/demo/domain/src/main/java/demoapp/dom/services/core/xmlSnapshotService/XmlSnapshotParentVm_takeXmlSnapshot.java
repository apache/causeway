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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.xml.XmlService;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.applib.value.Clob;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class XmlSnapshotParentVm_takeXmlSnapshot {

    @Inject XmlSnapshotService xmlSnapshotService;
    @Inject XmlService xmlService;
    // ...

//end::class[]

    private final XmlSnapshotParentVm xmlSnapshotParentVm;

//tag::PathsToInclude[]
    public enum PathsToInclude {
        NONE,
        PEER("peer"),
        CHILDREN("children"),
        PEER_AND_CHILDREN("peer", "children"),
        PEER_AND_ITS_CHILDREN("peer/children");

        final List<String> paths;
        PathsToInclude(String... paths) {
            this.paths = Collections.unmodifiableList(Arrays.asList(paths));
        }
    }
//end::PathsToInclude[]

//tag::SnapshotType[]
    public enum SnapshotType {
        XML,
        XSD
    }
//end::SnapshotType[]

//tag::class[]
    public Clob act(
            final PathsToInclude pathsToInclude,
            final SnapshotType snapshotType) {
        val builder = xmlSnapshotService.builderFor(xmlSnapshotParentVm);
        for (String path : pathsToInclude.paths) {
            builder.includePath(path);
        }
        val snapshot = builder.build();
        val doc = snapshotType == SnapshotType.XML
                ? snapshot.getXmlDocument() : snapshot.getXsdDocument();
        val fileName = String.format("%s.%s", pathsToInclude.name(), snapshotType.name().toLowerCase());
        return asClob(fileName, xmlService.asString(doc));
    }
//end::class[]

    public PathsToInclude default0Act() {
        return PathsToInclude.NONE;
    }
    public SnapshotType default1Act() {
        return SnapshotType.XML;
    }

    private static Clob asClob(final String fileName, final String xmlStr) {
        return new Clob(fileName, "application/xml", xmlStr);
    }
//tag::class[]
    // ...
}
//end::class[]
