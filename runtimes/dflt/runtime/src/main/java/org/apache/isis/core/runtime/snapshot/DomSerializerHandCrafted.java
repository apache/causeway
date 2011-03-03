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


package org.apache.isis.core.runtime.snapshot;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public final class DomSerializerHandCrafted implements DomSerializer {

    public String serialize(final Element domElement) {
        final StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        return buf.toString();
    }

    private void serializeToBuffer(final Element el, final StringBuffer buf) {
        buf.append("<").append(el.getTagName());
        final NamedNodeMap attributeMap = el.getAttributes();
        for (int i = 0; i < attributeMap.getLength(); i++) {
            final org.w3c.dom.Node node = attributeMap.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                final Attr attr = (Attr) node;
                buf.append(" ").append(attr.getName()).append("=\"").append(attr.getValue()).append("\"");
            }
        }
        buf.append(">");
        final NodeList nodeList = el.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final org.w3c.dom.Node node = nodeList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                final Text text = (Text) node;
                buf.append(text.getData());
            } else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                final Element childEl = (Element) node;
                serializeToBuffer(childEl, buf);
            }
        }
        buf.append("</").append(el.getTagName()).append(">");
    }

    public void serializeTo(final Element domElement, final OutputStream os) {
        final StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        final PrintStream printer = new PrintStream(os);
        printer.println(buf.toString());
        printer.flush();
    }

    public void serializeTo(final Element domElement, final Writer w) {
        final StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        final PrintWriter printer = new PrintWriter(w);
        printer.println(buf.toString());
        printer.flush();
    }

}
