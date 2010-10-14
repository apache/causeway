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


package org.apache.isis.extensions.berkeley;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;


public class DataFile {
    private static final int BLOCK_SIZE = 512;
    private RandomAccessFile file;
    private int nextBlock = 2;

    public static void main(String[] args) throws IOException {
        new DataFile().dump();
    }
    
    public DataFile() {
        try {
            file = new RandomAccessFile("data.txt", "rw");
            
            file.write(new byte[BLOCK_SIZE * 3]);
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void dump() throws IOException {
        System.out.println("serial number: " + loadSerialNumber());
        seekBlock(1);
        
        int i = 1;
        while(true) {
            System.out.println(i++ + file.readLong());
        }
    }
    
    public void close() {
        try {
            file.close();
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void insert(ObjectAdapter object) {
        long serialNumber = ((SerialOid) object.getOid()).getSerialNo();
        int block = nextBlock++;
        writeObject(block, object, serialNumber);
        writeId(block, serialNumber);
    }

    private void writeId(int block, long serialNumber) {
        try {
            file.seek(BLOCK_SIZE * 1 + serialNumber * 4);
            file.writeLong(block);
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }
    private void writeObject(int block, ObjectAdapter object, long serialNumber) {
        try {
            seekBlock(block);
            ObjectSpecification specification = object.getSpecification();
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
            buffer.put(specification.getFullName().getBytes());
            buffer.put((byte) '\n');
            buffer.put((serialNumber + "").getBytes());
            buffer.put((byte) '\n');

            ObjectAssociation[] associations = specification.getAssociations();
            for (ObjectAssociation association : associations) {
                ObjectAdapter property = association.get(object);
                if (property != null) {
                    EncodableFacet encodable = property.getSpecification().getFacet(EncodableFacet.class);
                    if (encodable != null) {
                        String data = encodable.toEncodedString(property);
                        buffer.put(data.getBytes());
                    }
                }
                buffer.put((byte) '\n');
            }

            long length = file.length();
            file.seek(length);
            file.write(buffer.array());

        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    public void saveSerialNumber(long serialNumber) {
        try {
            seekBlock(0);
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
            buffer.put(Long.toString(serialNumber).getBytes());
            buffer.put((byte) '\n');
            
            file.write(buffer.array());
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }
    
    public long loadSerialNumber() {
        try {
            seekBlock(0);
            String data = file.readLine();
            return data == null ? 1 : Long.getLong(data).longValue();
        } catch (IOException e) {
            throw new BerkeleyObjectStoreException(e);
        }
    }

    private void seekBlock(int block) throws IOException {
        file.seek(BLOCK_SIZE * block);
    }

}

