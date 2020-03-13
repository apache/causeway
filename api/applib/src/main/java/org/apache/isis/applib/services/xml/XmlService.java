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
package org.apache.isis.applib.services.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;

/**
 * This service provides a number of utility services for interacting with XML {@link Document}s
 */
// tag::refguide[]
public interface XmlService {

    // tag::refguide-3[]
    class Exception extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public Exception() {
            super();
        }

        public Exception(String message, Throwable cause) {
            super(message, cause);
        }

        public Exception(String message) {
            super(message);
        }

        public Exception(Throwable cause) {
            super(cause);
        }
    }
    // end::refguide-3[]


    // end::refguide[]
    /**
     * Converts xml string into an {@link Document W3C Document}.
     *
     * @see #asString(Document)
     */
    // tag::refguide[]
    Document asDocument(String xmlStr);                         // <.>

    // end::refguide[]

    /**
     * Serializes a {@link Document W3C Document} back into a string.
     *
     * @see #asDocument(String)
     */
    // tag::refguide[]
    public String asString(final Document doc);                 // <.>

    // end::refguide[]
    /**
     * Convenience method to walk XML document.
     */
    // tag::refguide[]
    Element getChildElement(                                    // <.>
                    final Element el, final String tagname);

    // end::refguide[]
    /**
     * Convenience method to obtain value of child text node.
     */
    // tag::refguide[]
    String getChildTextValue(final Element el);                 // <.>

}
// end::refguide[]
