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
package org.apache.isis.core.metamodel.util.snapshot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Stateless utility methods for manipulating XML documents.
 */
final class Helper {

    /**
     * Helper method
     */
    String trailingSlash(final String str) {
        return str.endsWith("/") ? str : str + "/";
    }

    /**
     * Utility method that returns just the class's name for the supplied fully
     * qualified class name.
     *
     * cf 'basename' in Unix.
     */
    String classNameFor(final String fullyQualifiedClassName) {
        final int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
        if (fullNameLastPeriodIdx > 0 && fullNameLastPeriodIdx < fullyQualifiedClassName.length()) {
            return fullyQualifiedClassName.substring(fullNameLastPeriodIdx + 1);
        } else {
            return fullyQualifiedClassName;
        }
    }

    /**
     * Utility method that returns the package name for the supplied fully
     * qualified class name, or <code>default</code> if the class is in no
     * namespace / in the default namespace.
     *
     * cf 'dirname' in Unix.
     */
    String packageNameFor(final String fullyQualifiedClassName) {
        final int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
        if (fullNameLastPeriodIdx > 0) {
            return fullyQualifiedClassName.substring(0, fullNameLastPeriodIdx);
        } else {
            return "default"; // TODO: should provide a better way to specify
            // namespace.
        }
    }

    /**
     * Returns the root element for the element by looking up the owner document
     * for the element, and from that its document element.
     *
     * If no document element exists, just returns the supplied document.
     */
    Element rootElementFor(final Element element) {
        final Document doc = element.getOwnerDocument();
        if (doc == null) {
            return element;
        }
        final Element rootElement = doc.getDocumentElement();
        if (rootElement == null) {
            return element;
        }
        return rootElement;
    }

    Document docFor(final Element element) {
        return element.getOwnerDocument();
    }

}
