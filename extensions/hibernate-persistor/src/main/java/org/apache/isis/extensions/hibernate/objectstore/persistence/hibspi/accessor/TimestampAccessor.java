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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor;

import java.util.Date;

import org.apache.isis.extensions.hibernate.objectstore.metamodel.version.LongVersion;


/**
 * Accesses Isis update timestamp, possibly for use in optimistic locking.
 */
public class TimestampAccessor extends OptimisticLockAccessor {
    public static LongVersionAccessor TIMESTAMP_ACCESSOR = new LongVersionAccessor() {
        public Object get(final LongVersion version) {
            return version.getTime();
        }

        public Class<Date> getReturnType() {
            return Date.class;
        }

        public void set(final LongVersion version, final Object value) {
            version.setTime((Date) value);
        }
    };

    @Override
    protected LongVersionAccessor getLongVersionAccessor() {
        return TIMESTAMP_ACCESSOR;
    }
}
