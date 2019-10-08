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

package org.apache.isis.metamodel.util.snapshot;

import org.w3c.dom.Element;

import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Represents a place in the graph to be navigated; really just wraps an object
 * and an XML Element in its XML document. Also provides the capability to
 * extract the corresponding XSD element (associated with each XML element).
 *
 * The XML element (its children) is mutated as the graph of objects is
 * navigated.
 */
final class Place {
    private static final String USER_DATA_XSD_KEY = "XSD";
    private final ManagedObject object;
    private final Element element;

    Place(final ManagedObject object, final Element element) {
        this.object = object;
        this.element = element;
    }

    public Element getXmlElement() {
        return element;
    }

    public ManagedObject getObject() {
        return object;
    }

    public Element getXsdElement() {
        final Object o = element.getUserData(USER_DATA_XSD_KEY);
        if (o == null || !(o instanceof Element)) {
            return null;
        }
        return (Element) o;
    }

    // TODO: smelly; where should this responsibility lie?
    static void setXsdElement(final Element element, final Element xsElement) {
        element.setUserData(USER_DATA_XSD_KEY, xsElement, null);
    }

}
