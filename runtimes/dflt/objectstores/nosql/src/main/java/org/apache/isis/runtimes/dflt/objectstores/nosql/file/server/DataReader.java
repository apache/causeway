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


package org.apache.isis.runtimes.dflt.objecstores.nosql.file.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


public class DataReader {
    private static final Logger LOG = Logger.getLogger(DataReader.class);

    private BufferedReader reader;
    private String id;
    private String version;

    /**
     * Opens the file for the specified id. The top line contains: type id version newline The remainder
     * contains the data.
     */
    public DataReader(String type, String id) throws IOException {
        File file = Util.dataFile(type, id);
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        String line = reader.readLine();
        if (line == null || line.length() == 0) {
            throw new FileServerException("No data in file: " + file.getAbsolutePath());
        }
        String[] split = line.split(" ");
        this.id = split[1];
        if (!id.equals(id)) {
            throw new FileServerException("Id in file (" + this.id + ") not the same as the file name: " + file.getAbsolutePath());
        }
        version = split[2];
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("Failed to close reader " + reader, e);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getData() {
        try {
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            throw new FileServerException("Failed to read data for " + id, e);
        }
    }
}

