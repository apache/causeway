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
package demoapp.dom.services.xmlSnapshotService;

import java.io.StringWriter;

import javax.inject.Inject;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.applib.value.Clob;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class XmlSnapshotParentVm_takeSnapshot {

    @Inject
    XmlSnapshotService xmlSnapshotService;

    // ...
//end::class[]

    private final XmlSnapshotParentVm xmlSnapshotParentVm;


//tag::class[]
    public Clob act(Demo demo) {
        return demo.snapshotUsing(xmlSnapshotService, xmlSnapshotParentVm);
    }

    public Demo default0Act() {
        return Demo.VANILLA;
    }

    public enum Demo {
        VANILLA {
            @Override
            void refine(XmlSnapshotService.Snapshot.Builder builder) {
            }
        }
        , WITH_PEER {
            @Override
            void refine(XmlSnapshotService.Snapshot.Builder builder) {
                builder.includePath("peer");
            }
        }
        , WITH_CHILDREN {
            @Override
            void refine(XmlSnapshotService.Snapshot.Builder builder) {
                builder.includePath("children");
            }
        }
        , WITH_PEER_AND_CHILDREN {
            @Override
            void refine(XmlSnapshotService.Snapshot.Builder builder) {
                builder.includePath("peer");
                builder.includePath("children");
            }
        };

        public final Clob snapshotUsing(XmlSnapshotService xmlSnapshotService, Object parentVm) {
            val builder = xmlSnapshotService.builderFor(parentVm);
            refine(builder);
            XmlSnapshotService.Snapshot snapshot = builder.build();
            val doc = snapshot.getXmlDocument();
            return Demo.asClob(doc, this);
        }

        abstract void refine(XmlSnapshotService.Snapshot.Builder builder);

        private static Clob asClob(final Document document, final Demo demo) {
            return new Clob(demo.name() + ".xml", "application/xml", asChars(document));
        }

        @SneakyThrows
        private static CharSequence asChars(Document document) {
            val domSource = new DOMSource(document);
            val writer = new StringWriter();
            val result = new StreamResult(writer);
            val tf = TransformerFactory.newInstance();
            val transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        }
    }
}
//end::class[]
