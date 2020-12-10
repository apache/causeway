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
package org.apache.isis.applib.services.xmlsnapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This service allows an XML document to be generated capturing the data of a root entity and specified related
 * entities.  This XML can be used for various purposes, such as mail merge/reporting, or adhoc auditing.
 *
 * <p>
 * The framework provides an implementation of this service (<tt>XmlSnapshotServiceDefault</tt>) which is automatically
 * registered and available for use; no further configuration is required.
 * </p>
 * @since 1.x {@index}
 */
// tag::refguide[]
public interface XmlSnapshotService {

    // end::refguide[]
    /**
     * @since 1.x {@index}
     */
    // tag::refguide-1[]
    interface Snapshot {
        // end::refguide-1[]
        /**
         *  Converts the snapshotted state into an XML document.
         */
        // tag::refguide-1[]
        Document getXmlDocument();          // <.>
        // end::refguide-1[]
        /**
         *  Creates a corresponding XSD that describes the structure of the exported XML.
         */
        // tag::refguide-1[]
        Document getXsdDocument();          // <.>
        // end::refguide-1[]
        /**
         * @since 1.x {@index}
         */
        // tag::refguide-2[]
        interface Builder {
            // end::refguide-2[]
            /**
             * Enrich the snapshot to include the state of these referenced objects 
             */
            // tag::refguide-2[]
            void includePath(final String path);                    // <.>
            // end::refguide-2[]
            /**
             * Ditto, but add an XML annotation attribute to the included element(s).
             */
            // tag::refguide-2[]
            void includePathAndAnnotation(                          // <.>
                final String path, final String annotation);
            // end::refguide-2[]
            /**
             * Builds the Snapshot.
             */
            // tag::refguide-2[]
            XmlSnapshotService.Snapshot build();                    // <.>
        }
        // end::refguide-2[]
        // tag::refguide-1[]
    }
    // end::refguide-1[]

    /**
     *  Exports the state of a domain object into a Snapshot (which can then be converted into XML, for example).
     */
    // tag::refguide[]
    XmlSnapshotService.Snapshot snapshotFor(                    // <.>
                                    final Object domainObject);

    /**
     *  Creates a Snapshot.Builder that allows the contents of the snapshot to include other related state. 
     */
    XmlSnapshotService.Snapshot.Builder builderFor(             // <.>
                                    final Object domainObject);

    // end::refguide[]

    /**
     * Convenience method to extract value of an XML element, based on its type.
     */
    // tag::refguide[]
    <T> T getChildElementValue(                                 // <.>
            final Element el, final String tagname,
            final Class<T> expectedCls);


}
// end::refguide[]
