package org.nakedobjects.utility.xmlsnapshot;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.crimson.tree.ElementNode2;
import org.apache.crimson.tree.XmlWriteContext;
import org.w3c.dom.Element;

public class DomSerializerCrimson implements DomSerializer {

    private static ElementNode2 assertCrimson(final Element domElement) {
        if (!(domElement instanceof ElementNode2)) {
            throw new IllegalArgumentException("Not using Crimson");
        }
        return (ElementNode2)domElement;
    }
    
    public String serialize(Element domElement) {
        CharArrayWriter caw = new CharArrayWriter();
        try {
            serializeTo(domElement, caw);
            return caw.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public void serializeTo(Element domElement, OutputStream os) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(os);
        serializeTo(domElement, osw);
    }

    public void serializeTo(Element domElement, Writer w) throws IOException {
        ElementNode2 el = assertCrimson(domElement);
        XmlWriteContext xwc = new XmlWriteContext(w, 2);
        el.writeXml(xwc);
        xwc.getWriter().flush();
     }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/