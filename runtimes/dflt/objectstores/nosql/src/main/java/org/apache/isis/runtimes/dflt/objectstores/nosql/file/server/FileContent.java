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


package org.apache.isis.runtimes.dflt.objectstores.nosql.file.server;

class FileContent {

    final char command;
    final String id;
    final String currentVersion;
    final String newVersion;
    final String data;
    final String type;

    public FileContent(char command, String id, String currentVersion, String newVersion, String type, String buf) {
        this.command = command;
        this.id = id;
        this.currentVersion = currentVersion;
        this.newVersion = newVersion;
        this.type = type;
        this.data = buf;
    }

}

