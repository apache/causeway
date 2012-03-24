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
package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.io.IOException;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.directly.DirectlyStringableOidWithSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;

public class NoSqlOid implements DirectlyStringableOidWithSpecification {

    private SerialOid serialOid;
    private String className;
    
    public NoSqlOid(String className, SerialOid serialOid) {
        this.className = className;
        this.serialOid = serialOid;
    }

    // ////////////////////////////////////////////
    // Oid
    // ////////////////////////////////////////////

    @Override
    public void copyFrom(Oid oid) {
        NoSqlOid other = (NoSqlOid) oid;
        this.serialOid = other.serialOid;
    }

    @Override
    public Oid getPrevious() {
        return new NoSqlOid(className, (SerialOid) serialOid.getPrevious());
    }

    @Override
    public boolean hasPrevious() {
        return serialOid.hasPrevious();
    }

    @Override
    public void clearPrevious() {
        serialOid.clearPrevious();
    }

    @Override
    public boolean isTransient() {
        return serialOid.isTransient();
    }

    @Override
    public void makePersistent() {
        serialOid.makePersistent();
    }

    // ////////////////////////////////////////////
    // Encodable (Oid)
    // ////////////////////////////////////////////

    public NoSqlOid(final DataInputExtended input) throws IOException {
        this.className = input.readUTF();
        this.serialOid = new SerialOid(input);
    }

    @Override
    public void encode(DataOutputExtended outputStream) throws IOException {
        outputStream.writeUTF(className);
        serialOid.encode(outputStream);
    }

    // ////////////////////////////////////////////
    // OidWithSpecification
    // ////////////////////////////////////////////

    @Override
    public String getClassName() {
        return className;
    }

    // ////////////////////////////////////////////
    // Delegate to underlying SerialOid
    // ////////////////////////////////////////////

    public long getSerialNo() {
        return serialOid.getSerialNo();
    }

    public void setId(long persistentId) {
        serialOid.setId(persistentId);
    }

    // ////////////////////////////////////////////
    // Directly Stringable
    // ////////////////////////////////////////////

    public static NoSqlOid deString(String oidStr) {
        final String[] split = oidStr.split("~");
        return new NoSqlOid(split[0], SerialOid.deString(split[1]));
    }
    
    @Override
    public String enString() {
        return className + "~" + serialOid.enString();
    }




}
