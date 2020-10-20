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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus.typeconverters.applib;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

import org.datanucleus.store.types.converters.TypeConverter;

public class IsoOffsetTimeConverter implements TypeConverter<OffsetTime, String>{

    private static final long serialVersionUID = 1L;
    
    @Override
    public String toDatastoreType(final OffsetTime offsetTime) {
        return offsetTime != null
                ? offsetTime.format(DateTimeFormatter.ISO_OFFSET_TIME)
                : null;
    }

    @Override
    public OffsetTime toMemberType(final String datastoreValue) {
        return datastoreValue != null
                ? OffsetTime.parse(datastoreValue, DateTimeFormatter.ISO_OFFSET_TIME)
                : null;
    }

}
