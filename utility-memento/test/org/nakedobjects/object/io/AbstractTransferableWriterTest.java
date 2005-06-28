package org.nakedobjects.object.io;

import java.util.Vector;

import junit.framework.TestCase;


public class AbstractTransferableWriterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AbstractTransferableWriterTest.class);
    }

    private Vector output = new Vector();
    private AbstractTransferableWriter writer;

    protected void setUp() throws Exception {
        writer = new AbstractTransferableWriter() {

            public void close() {}

            public void writeInt(int i) {}

            public void writeLong(long value) {}

            public void writeString(String string) {
                output.addElement(string);
            }
        };
    }

    public void testWriteObject() {
        writer.writeString("test");
        assertEquals("test", output.elementAt(0));
        assertEquals(1, output.size());
    }

    public void testWriteObject2() {
        Transferable object = new Transferable() {
            public void writeData(TransferableWriter writer) {
                writer.writeString("transferrable object");
            }
        };

        writer.writeObject(object);

        assertEquals("org.nakedobjects.object.io.AbstractTransferableWriterTest$2", output.elementAt(0));
        assertEquals("transferrable object", output.elementAt(1));
        assertEquals(2, output.size());
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