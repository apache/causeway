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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.isis.objectstore.nosql.NoSqlStoreException;
import org.apache.isis.objectstore.nosql.db.StateReader;

public class JsonStateReader implements StateReader {
    
    // private static final Logger LOG = LoggerFactory.getLogger(FileStateReader.class);
    
    private JSONObject instance;

    public JsonStateReader(final String data) {
        try {
            final JSONObject instance = new JSONObject(data);
            this.instance = instance;
        } catch (final JSONException e) {
            throw new NoSqlStoreException("failed initialise JSON object for text form: " + data, e);
        }
    }

    private JsonStateReader(final JSONObject aggregatedObject) {
        instance = aggregatedObject;
    }

    @Override
    public StateReader readAggregate(final String name) {
        if (instance.has(name)) {
            final JSONObject aggregatedObject = instance.optJSONObject(name);
            if (aggregatedObject == null) {
                return null;
            } else {
                return new JsonStateReader(aggregatedObject);
            }
        } else {
            return null;
        }
    }

    @Override
    public long readLongField(final String id) {
        final Object value = instance.opt(id);
        if (value == null) {
            return 0;
        } else {
            return Long.valueOf((String) value);
        }
    }

    @Override
    public String readField(final String name) {
        if (instance.has(name)) {
            final Object value = instance.optString(name);
            if (value == null) {
                return null;
            } else {
                return (String) value;
            }
        } else {
            return null;
        }
    }

//    @Override
//    public String readObjectType() {
//        return readRequiredField("_type");
//    }
//
//    @Override
//    public String readId() {
//        return readRequiredField("_id");
//    }

      @Override
      public String readOid() {
          return readRequiredField(PropertyNames.OID);
      }

    @Override
    public String readVersion() {
        return readRequiredField(PropertyNames.VERSION);
    }

    @Override
    public String readEncrytionType() {
        try {
            String encryptionType;
            if (instance.has("_encrypt")) {
                encryptionType = instance.getString("_encrypt");
            } else {
                encryptionType = "none";
            }
            return encryptionType;
        } catch (final JSONException e) {
            throw new NoSqlStoreException("failed to read field _encrypt", e);
        }
    }

    @Override
    public String readUser() {
        return readRequiredField(PropertyNames.USER);
    }

    @Override
    public String readTime() {
        return readRequiredField(PropertyNames.TIME);
    }

    private String readRequiredField(final String name) {
        try {
            final Object value = instance.get(name);
            return (String) value;
        } catch (final JSONException e) {
            throw new NoSqlStoreException("failed to read field " + name, e);
        }
    }

    @Override
    public List<StateReader> readCollection(final String id) {
        final JSONArray array = instance.optJSONArray(id);
        final List<StateReader> readers = new ArrayList<StateReader>();
        if (array != null) {
            final int size = array.length();
            for (int i = 0; i < size; i++) {
                readers.add(new JsonStateReader(array.optJSONObject(i)));
            }
        }
        return readers;
    }
}
