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


package org.apache.isis.alternatives.objectstore.nosql.file;

import org.apache.isis.alternatives.objectstore.nosql.NoSqlCommandContext;
import org.apache.isis.alternatives.objectstore.nosql.StateWriter;


class FileClientCommandContext implements NoSqlCommandContext {

    private final ClientConnection connection;

    public FileClientCommandContext(ClientConnection connection) {
        this.connection = connection;
    }

    public void start() {
    }

    public void end() {
    }

    public StateWriter createStateWriter(String specificationName) {
        return new JsonStateWriter(connection, specificationName);
    }

    public void delete(String specificationName, String key, String version) {
        connection.request('D', specificationName + " " + key + " " + version + " null");
        connection.endRequestSection();
    }

    public void insert(StateWriter writer) {
        write('I', (JsonStateWriter) writer);
    }

    public void update(StateWriter writer) {
        write('U', (JsonStateWriter) writer);
    }

    private void write(char command, JsonStateWriter writer) {
        connection.request(command, writer.getRequest());
        connection.requestData(writer.getData());
        connection.endRequestSection();
    }

}

