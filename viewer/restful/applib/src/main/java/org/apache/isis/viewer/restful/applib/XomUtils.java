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
package org.apache.isis.viewer.restful.applib;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Serializer;

public final class XomUtils {

    public static String getAttributeValueElseException(final Document doc, final String xpath, final String msg) {
        final Attribute attribute = getAttributeElseException(doc, xpath, msg);
        return attribute.getValue();
    }

    private static Attribute getAttributeElseException(final Document document, final String xpath, final String msg) {
        final Nodes query = document.getRootElement().query(xpath);
        if (query.size() != 1) {
            throw new IllegalArgumentException(msg);
        }
        final Node node = query.get(0);
        if (!(node instanceof Attribute)) {
            throw new IllegalArgumentException(msg);
        }
        return (Attribute) node;
    }

    public static void prettyPrint(final Document doc) throws IOException {
        final Serializer serializer = new Serializer(System.out, Constants.URL_ENCODING_CHAR_SET);
        serializer.setIndent(4);
        serializer.setMaxLength(64);
        serializer.setPreserveBaseURI(true);
        serializer.write(doc);
        serializer.flush();
    }

    private XomUtils() {
    }

}
