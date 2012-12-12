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

package org.apache.isis.objectstore.nosql.db.file;

import java.util.zip.CRC32;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.objectstore.nosql.NoSqlCommandContext;
import org.apache.isis.objectstore.nosql.db.StateWriter;

class FileClientCommandContext implements NoSqlCommandContext {

    private final ClientConnection connection;

    public FileClientCommandContext(final ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void start() {
    }

    @Override
    public void end() {
    }

    @Override
    public StateWriter createStateWriter(final ObjectSpecId specificationName) {
        return new JsonStateWriter();
    }

    @Override
    public void delete(final ObjectSpecId objectSpecId, final String key, final String version, final Oid oid) {
        connection.request('D', objectSpecId + " " + key + " " + version + " null");
        connection.endRequestSection();
    }

    @Override
    public void insert(final StateWriter writer) {
        write('I', (JsonStateWriter) writer);
    }

    @Override
    public void update(final StateWriter writer) {
        write('U', (JsonStateWriter) writer);
    }

    private void write(final char command, final JsonStateWriter writer) {
        connection.request(command, writer.getRequest());
        final String data = writer.getData();

        final CRC32 inputChecksum = new CRC32();
        inputChecksum.update(data.getBytes());
        inputChecksum.update('\n');
        final long checksum = inputChecksum.getValue();
        final String code = Long.toHexString(checksum);

        connection.requestData("00000000".substring(0, 8 - code.length()) + code + data);
        connection.endRequestSection();
    }

}
