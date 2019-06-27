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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.internal.encoding.DataInputExtended;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;
import org.apache.isis.commons.internal.encoding.Encodable;
import org.apache.isis.metamodel.adapter.oid.Oid;

public class ObjectData extends Data {

    private static final long serialVersionUID = 7121411963269613347L;
    private final static Encodable NO_ENTRY = new Null();
    private final Map<String, Object> fields = new HashMap<String, Object>();

    private static enum As {
        OBJECT(0), NULL(1), STRING(2);
        static Map<Integer, As> cache = new HashMap<Integer, As>();
        static {
            for (final As as : values()) {
                cache.put(as.idx, as);
            }
        }
        private final int idx;

        private As(final int idx) {
            this.idx = idx;
        }

        static As get(final int idx) {
            return cache.get(idx);
        }

        public static As readFrom(final DataInputExtended input) throws IOException {
            return get(input.readByte());
        }

        public void writeTo(final DataOutputExtended output) throws IOException {
            output.writeByte(idx);
        }
    }

    public ObjectData(final Oid oid, final String className) {
        super(oid, className);
        initialized();
    }

    public ObjectData(final DataInputExtended input) throws IOException {
        super(input);

        final int size = input.readInt();
        for (int i = 0; i < size; i++) {
            final String key = input.readUTF();
            final As as = As.readFrom(input);
            if (as == As.OBJECT) {
                final Data object = input.readEncodable(Data.class);
                fields.put(key, object);
            } else if (as == As.NULL) {
                fields.put(key, NO_ENTRY);
            } else {
                final String value = input.readUTF();
                fields.put(key, value);
            }
        }
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);

        output.writeInt(fields.size());

        for (final String key : fields.keySet()) {
            final Object value = fields.get(key);

            output.writeUTF(key);
            if (value instanceof Data) {
                As.OBJECT.writeTo(output);
                output.writeEncodable(value);
            } else if (value instanceof Null) {
                As.NULL.writeTo(output);
                // nothing to do; if read back corresponds to NO_ENTRY
            } else {
                As.STRING.writeTo(output);
                output.writeUTF((String) value);
            }
        }
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

    public void addField(final String fieldName, final Object entry) {
        if (fields.containsKey(fieldName)) {
            throw new IllegalArgumentException("Field already entered " + fieldName);
        }
        fields.put(fieldName, entry == null ? NO_ENTRY : entry);
    }

    public boolean containsField() {
        return fields != null && fields.size() > 0;
    }

    public Object getEntry(final String fieldName) {
        final Object entry = fields.get(fieldName);
        return entry == null || entry.getClass() == NO_ENTRY.getClass() ? null : entry;
    }

    @Override
    public String toString() {
        return fields.toString();
    }


}
