package org.nakedobjects.object.io;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BinaryTransferableWriter extends AbstractTransferableWriter {
    private DataOutputStream dataOutputStream;
    private ByteArrayOutputStream byteArray;
    
    public BinaryTransferableWriter() {
        byteArray = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStream(byteArray);
    }

    public void writeInt(int i) {
        try {
            dataOutputStream.writeInt(i);
        } catch (IOException e) {
            throw new TransferableException(e);
        }
    }

    public void writeString(String string) {
        try {
            dataOutputStream.writeUTF(string);
        } catch (IOException e) {
            throw new TransferableException(e);
        }
    }
    
    public byte[] getBinaryData() {
        byte[] bytes = byteArray.toByteArray();
        try {
            	dataOutputStream.close();
            } catch (IOException e) {
                throw new TransferableException(e);
            } 
        return bytes;
    }

    public void writeLong(long value) {
        try {
            dataOutputStream.writeLong(value);
        } catch (IOException e) {
            throw new TransferableException(e);
        }    
    }

    public void close() {
        try {
            dataOutputStream.close();
            byteArray.close();
        } catch (IOException e) {
            throw new NakedObjectRuntimeException();
        }
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