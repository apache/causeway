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
package org.apache.causeway.applib.services.xmlsnapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Allows an XML document to be generated capturing the data of a root entity
 * and specified related entities.
 *
 * <p>
 *     An XSD schema (to which the XML will be compatible) can also be
 *     generated.
 * </p>
 *
 * <p>
 *     This XML/XSD can be used for various purposes, such as mail
 *     merge/reporting, or adhoc auditing.
 * </p>
 *
 * <p>
 *     The service offers a basic API to create a snapshot of a single object,
 *     and a more flexible API that allows the size of the graph to be
 *     customized.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface XmlSnapshotService {


    /**
     * @since 1.x {@index}
     */
    interface Snapshot {
        /**
         *  Converts the snapshotted state into an XML document.
         */
        Document getXmlDocument();
        /**
         *  Creates a corresponding XSD that describes the structure of the exported XML.
         */
        Document getXsdDocument();
        /**
         * @since 1.x {@index}
         */
        interface Builder {
            /**
             * Enrich the snapshot to include the state of these referenced objects
             */
            void includePath(final String path);
            /**
             * Ditto, but add an XML annotation attribute to the included element(s).
             */
            void includePathAndAnnotation(
                final String path, final String annotation);
            /**
             * Builds the Snapshot.
             */
            XmlSnapshotService.Snapshot build();
        }
    }

    /**
     *  Exports the state of a domain object into a Snapshot (which can then be converted into XML, for example).
     */
    XmlSnapshotService.Snapshot snapshotFor(
                                    final Object domainObject);

    /**
     *  Creates a Snapshot.Builder that allows the contents of the snapshot to include other related state.
     */
    XmlSnapshotService.Snapshot.Builder builderFor(
                                    final Object domainObject);


    /**
     * Convenience method to extract value of an XML element, based on its type.
     */
    <T> T getChildElementValue(
            final Element el, final String tagname,
            final Class<T> expectedCls);


}
