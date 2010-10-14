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


package org.apache.isis.extensions.file;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.isis.extensions.nosql.NoSqlStoreException;
import org.apache.isis.extensions.nosql.StateWriter;


public class JsonStateWriter implements StateWriter {

    // private static final Logger LOG = Logger.getLogger(FileStateWriter.class);

    private JSONObject dbObject;
    private String type;
    private String oid;
    private String currentVersion;
    private String newVersion;

    public JsonStateWriter(ClientConnection connection, String specName) {
        dbObject = new JSONObject();
    }

    public StateWriter addAggregate(String id) {
        JsonStateWriter jsonStateWriter = new JsonStateWriter(null, null);
        try {
            dbObject.put(id, jsonStateWriter.dbObject);
        } catch (JSONException e) {
            throw new NoSqlStoreException(e);
        }
        return jsonStateWriter;
    }
    
    public void writeType(String type) {
        this.type = type;
        writeField("_type", type);
    }

    public void writeId(String oid) {
        this.oid = oid;
        writeField("_id", oid);
    }

    public void writeVersion(String currentVersion, String newVersion) {
        this.currentVersion = currentVersion;
        this.newVersion = newVersion;
        writeField("_version", newVersion);
    }

    public void writeTime(String time) {
        writeField("_time", time);
    }

    public void writeUser(String user) {
        writeField("_user", user);
    }

    public void writeField(String id, String data) {
        try {
            dbObject.put(id, data == null ? JSONObject.NULL : data);
        } catch (JSONException e) {
            throw new NoSqlStoreException(e);
        }
    }

    public void writeField(String id, long l) {
        try {
            dbObject.put(id, Long.toString(l));
        } catch (JSONException e) {
            throw new NoSqlStoreException(e);
        }
    }

    public String getRequest() {
        return type + " " + oid + " " + currentVersion + " " + newVersion;
    }

    public String getData() {
        try {
            return dbObject.toString(4);
        } catch (JSONException e) {
            throw new NoSqlStoreException(e);
        }
    }

    public StateWriter createElementWriter() {
        return new JsonStateWriter(null, null);
    }
    
    public void writeCollection(String id, List<StateWriter> elements) {
        ArrayList<JSONObject> collection = new ArrayList<JSONObject>();
        for (StateWriter writer : elements) {
            collection.add(((JsonStateWriter) writer).dbObject);
        }
        try {
            dbObject.put(id, collection);
        } catch (JSONException e) {
            throw new NoSqlStoreException(e);
        }
    }
}

