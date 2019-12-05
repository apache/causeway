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

package org.apache.isis.runtime.memento;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.metamodel.adapter.oid.Oid;

import lombok.NoArgsConstructor;

final class ObjectData extends Data {

    private static final long serialVersionUID = 3772154051989942219L;
    
    private final Map<String, Object> fields = new HashMap<String, Object>();

    public ObjectData(final Oid oid, final String className) {
        super(oid, className);
    }

    public void addField(final String fieldName, final Object entry) {
        if (fields.containsKey(fieldName)) {
            throw new IllegalArgumentException("Field already entered " + fieldName);
        }
        fields.put(fieldName, entry == null ? NO_ENTRY : entry);
    }

    public boolean containsField() {
        return !fields.isEmpty();
    }

    public Object getEntry(final String fieldName) {
        final Object entry = fields.get(fieldName);
        return entry == null || entry.getClass() == NO_ENTRY.getClass() ? null : entry;
    }

    @Override
    public String toString() {
        return fields.toString();
    }
    
    // -- HELPER
    
    private final static Serializable NO_ENTRY = new EmptyEntry();
    
    @NoArgsConstructor
    private final static class EmptyEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
            return "NULL";
        }

    }


}
