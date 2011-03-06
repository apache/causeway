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


package org.apache.isis.runtimes.dflt.objecstores.nosql.file;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.isis.runtimes.dflt.objecstores.nosql.NoSqlStoreException;
import org.apache.isis.runtimes.dflt.objecstores.nosql.StateReader;


public class JsonStateReader implements StateReader {
//    private static final Logger LOG = Logger.getLogger(FileStateReader.class);
    private JSONObject instance;

    public JsonStateReader(String data) {
          try {  
            JSONObject instance = new JSONObject(data);
            this.instance = instance;
        } catch (JSONException e) {
            throw new NoSqlStoreException("failed initialise JSON object for text form: " + data, e);
        }
    }
    
    private JsonStateReader(JSONObject aggregatedObject) {
        instance = aggregatedObject;
    }

    public StateReader readAggregate(String name) {
        if (instance.has(name)) {
            JSONObject aggregatedObject = instance.optJSONObject(name);
            if (aggregatedObject == null) {
                return null;
            } else {
                return new JsonStateReader(aggregatedObject);
            }
        } else {
            return null;
        }
    }
    
    public long readLongField(String id) {
        Object value = instance.opt(id);
        if (value == null) {
            return 0;
        } else {
            return Long.valueOf((String) value);
        }
    }

    public String readField(String name) {
        if (instance.has(name)) {
            Object value = instance.optString(name);
            if (value == null) {
                return null;
            } else {
                return (String) value;
            }
        } else {
            return null;
        }
    }

    public String readObjectType() {
        return readRequiredField("_type");
    }

    public String readId() {
        return readRequiredField("_id");
    }

    public String readVersion() {
        return readRequiredField("_version");
    }

    public String readUser() {
        return readRequiredField("_user");
    }

    public String readTime() {
        return readRequiredField("_time");
    }

    private String readRequiredField(String name) {
        try {
            Object value = instance.get(name);
            return (String) value;
        } catch (JSONException e) {
            throw new NoSqlStoreException("failed to read long field value", e);
        }
    }
    
    public List<StateReader> readCollection(String id) {
        JSONArray array = instance.optJSONArray(id);
        List<StateReader> readers = new ArrayList<StateReader>();
        int size = array.length();
        for (int i = 0; i < size; i++) {
            readers.add(new JsonStateReader(array.optJSONObject(i)));
        }
        return readers;
    }
}


