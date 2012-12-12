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

package org.apache.isis.objectstore.nosql.db.file.server;

import java.io.IOException;
import java.io.OutputStream;

class FileContent {

    private static final String ENCODING = "utf-8";

    final char command;
    final String id;
    final String currentVersion;
    final String newVersion;
    final String data;
    final String type;

    public FileContent(final char command, final String id, final String currentVersion, final String newVersion, final String type, final String buf) {
        this.command = command;
        this.id = id;
        this.currentVersion = currentVersion;
        this.newVersion = newVersion;
        this.type = type;
        this.data = buf;
    }

    public void write(final OutputStream output) throws IOException {
        output.write(type.getBytes(ENCODING));
        output.write(' ');
        output.write(id.getBytes(ENCODING));
        output.write(' ');
        output.write(newVersion.getBytes(ENCODING));
        output.write('\n');
        output.write(data.getBytes(ENCODING));
    }

}
