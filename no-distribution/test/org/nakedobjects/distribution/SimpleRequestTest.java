package org.nakedobjects.distribution;

import Serializable;

import org.nakedobjects.object.io.BinaryTransferableReader;
import org.nakedobjects.object.io.BinaryTransferableWriter;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class SimpleRequestTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimpleRequestTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
    }

    public void testRequest() {

        Request.init(new RequestFowarder() {

            public Serializable executeRemotely(Request request) {
                SimpleRequest clientsRequest = (SimpleRequest) request;
                
                // write out request
                BinaryTransferableWriter writer = new BinaryTransferableWriter();
                writer.writeString(clientsRequest.getClass().getName());
                clientsRequest.writeData(writer);
                byte[] transferableData = writer.getBinaryData();
                writer.close();

                // read in request and recreate the request object
                BinaryTransferableReader reader = new BinaryTransferableReader(transferableData);
                SimpleRequest serversCopyOfRequest = (SimpleRequest) reader.readObject();
                reader.close();
                
                assertNotSame("two copies", clientsRequest, serversCopyOfRequest);
                assertEquals("hello", serversCopyOfRequest.str);
                assertEquals(189L, serversCopyOfRequest.no);
                
                // create response
                writer = new BinaryTransferableWriter();
                
                // execute client's request on server
                serversCopyOfRequest.generateResponse(null);
                
                transferableData = writer.getBinaryData();
                writer.close();


                // read in request and recreate the request object
                reader = new BinaryTransferableReader(transferableData);
                clientsRequest.restoreResponse(reader);
               
                assertEquals("response abc", clientsRequest.myResponse);
             }

            public void init() {}

            public void shutdown() {}
        });

        SimpleRequest request = new SimpleRequest("hello", 189);
        request.execute();
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