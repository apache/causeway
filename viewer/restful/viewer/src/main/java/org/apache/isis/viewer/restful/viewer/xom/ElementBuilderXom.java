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
package org.apache.isis.viewer.restful.viewer.xom;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;


public class ElementBuilderXom {

    /**
     * The element being built.
     */
    private Element element;

    public ElementBuilderXom(final String name) {
        el(name);
    }

    /**
     * Creates an uninitialized builder.
     * 
     * <p>
     * Must be followed by a call to {@link #el(String)}.
     */
    public ElementBuilderXom() {}

    public ElementBuilderXom el(final String name) {
        assertNotInitialized();
        this.element = new Element(name);
        return this;
    }

    public ElementBuilderXom attr(final String name, final String value) {
        assertInitialized();
        element.addAttribute(new Attribute(name, value));
        return this;
    }

    public ElementBuilderXom classAttr(final String htmlClassAttribute) {
        if (htmlClassAttribute != null) {
            element.addAttribute(new Attribute("class", htmlClassAttribute));
        }
        return this;
    }

    public ElementBuilderXom idAttr(final String idAttribute) {
        if (idAttribute != null) {
            element.addAttribute(new Attribute("id", idAttribute));
        }
        return this;
    }

    public ElementBuilderXom append(final Node childNode) {
        element.appendChild(childNode);
        return this;
    }

    public ElementBuilderXom append(final String childText) {
        if (childText != null) {
            element.appendChild(childText);
        }
        return this;
    }

    public Element build() {
        return element;
    }

    private void assertInitialized() {
        if (element == null) {
            throw new IllegalStateException("Element name not specified");
        }
    }

    private void assertNotInitialized() {
        if (element != null) {
            throw new IllegalStateException("Element already specified");
        }
    }

}
