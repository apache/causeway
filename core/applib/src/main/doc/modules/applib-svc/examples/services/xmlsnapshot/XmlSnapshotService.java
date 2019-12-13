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

import org.apache.isis.applib.annotation.Programmatic;

/**
 * This service allows an XML document to be generated capturing the data of a root entity and specified related
 * entities.  This XML can be used for various purposes, such as mail merge/reporting, or adhoc auditing.
 *
 * <p>
 * The framework provides an implementation of this service (<tt>XmlSnapshotServiceDefault</tt>) which is automatically
 * registered and available for use; no further configuration is required.
 * </p>
 */
public interface XmlSnapshotService {

    public interface Snapshot {
        public Document getXmlDocument();
        public Document getXsdDocument();

        public String getXmlDocumentAsString();
        public String getXsdDocumentAsString();
    }

    public interface Builder {
        public void includePath(final String path);
        public void includePathAndAnnotation(final String path, final String annotation);
        public XmlSnapshotService.Snapshot build();
    }

    public static class Exception extends RuntimeException {

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

    @Programmatic
    public XmlSnapshotService.Snapshot snapshotFor(final Object domainObject);

    @Programmatic
    public XmlSnapshotService.Builder builderFor(final Object domainObject);

    /**
     * Convenience to convert xml string (eg as obtained by {@link Snapshot#getXmlDocumentAsString()} or
     * {@link Snapshot#getXsdDocumentAsString()}) back into a {@link Document W3C Document}.
     */
    @Programmatic
    public Document asDocument(String xmlStr);

    /**
     * Convenience method to extract value of an XML element, based on its type.
     */
    @Programmatic
    public <T> T getChildElementValue(final Element el, final String tagname, final Class<T> expectedCls);

    /**
     * Convenience method to walk XML document.
     */
    @Programmatic
    public Element getChildElement(final Element el, final String tagname);

    /**
     * Convenience method to obtain value of child text node.
     */
    @Programmatic
    public String getChildTextValue(final Element el);

}
