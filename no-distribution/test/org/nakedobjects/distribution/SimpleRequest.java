package org.nakedobjects.distribution;

import org.nakedobjects.object.io.TransferableReader;
import org.nakedobjects.object.io.TransferableWriter;


public class SimpleRequest extends Request {
    String myResponse;
    final long no;
    final String str;

    public SimpleRequest(String str, long no) {
        this.str = str;
        this.no = no;
    }

    public SimpleRequest(TransferableReader reader) {
        str = reader.readString();
        no = reader.readLong();
    }

    protected void generateResponse(RequestContext context) {
        responseWriter.writeString("response abc");
    }

    protected void restoreResponse(TransferableReader reader) {
        myResponse = reader.readString();
    }

    protected void writeRequestData(TransferableWriter writer) {
        writer.writeString(str);
        writer.writeLong(no);
    }
    
    public String toString() {
        return "SimpleRequest [str=" + str + ",response=" + myResponse + "]";
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