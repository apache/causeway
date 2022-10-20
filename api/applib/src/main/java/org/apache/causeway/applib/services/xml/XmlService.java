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
package org.apache.causeway.applib.services.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This service provides a number of utility services for interacting with
 * w3c DOM (XML) {@link Document}s
 *
 * @since 1.x {@index}
 */
public interface XmlService {


    /**
     * Converts xml string into an {@link Document W3C Document}.
     *
     * @see #asString(Document)
     */
    Document asDocument(String xmlStr);

    /**
     * Serializes a {@link Document W3C Document} back into a string.
     *
     * @see #asDocument(String)
     */
    public String asString(final Document doc);

    /**
     * Convenience method to walk XML document.
     */
    Element getChildElement(
                    final Element el, final String tagname);

    /**
     * Convenience method to obtain value of child text node.
     */
    String getChildTextValue(final Element el);

}
