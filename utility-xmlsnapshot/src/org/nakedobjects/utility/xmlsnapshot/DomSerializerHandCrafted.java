package org.nakedobjects.utility.xmlsnapshot;

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
        StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        return buf.toString();
    }

    private void serializeToBuffer(final Element el, StringBuffer buf) {
        buf.append("<").append(el.getTagName());
        NamedNodeMap attributeMap = el.getAttributes();
        for (int i = 0; i < attributeMap.getLength(); i++) {
            org.w3c.dom.Node node = attributeMap.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
                Attr attr = (Attr)node;
                buf.append(" ").append(attr.getName()).append("=\"").append(attr.getValue()).append("\"");
            }
        }
        buf.append(">");
        NodeList nodeList = el.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                Text text = (Text) node;
                buf.append(text.getData());
            }
            else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element childEl = (Element) node;
                serializeToBuffer(childEl, buf);
            }
        }
        buf.append("</").append(el.getTagName()).append(">");
    }

    public void serializeTo(final Element domElement, OutputStream os) {
        StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        PrintStream printer = new PrintStream(os);
        printer.println(buf.toString());
        printer.flush();
    }

    public void serializeTo(final Element domElement, Writer w) {
        StringBuffer buf = new StringBuffer();
        serializeToBuffer(domElement, buf);
        PrintWriter printer = new PrintWriter(w);
        printer.println(buf.toString());
        printer.flush();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */