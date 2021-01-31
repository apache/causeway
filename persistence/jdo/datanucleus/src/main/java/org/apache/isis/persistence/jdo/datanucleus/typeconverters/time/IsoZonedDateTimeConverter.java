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
package org.apache.isis.persistence.jdo.datanucleus.typeconverters.time;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.datanucleus.store.types.converters.TypeConverter;

/**
 * @since 2.0 {@index}
 */
public class IsoZonedDateTimeConverter implements TypeConverter<ZonedDateTime, String>{

    private static final long serialVersionUID = 1L;

    @Override
    public String toDatastoreType(final ZonedDateTime offsetTime) {
        return offsetTime != null
                ? offsetTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
                : null;
    }

    @Override
    public ZonedDateTime toMemberType(final String datastoreValue) {
        return datastoreValue != null
                ? ZonedDateTime.parse(datastoreValue, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                : null;
    }

}
