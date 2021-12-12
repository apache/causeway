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
package org.apache.isis.extensions.viewer.wicket.exceldownload.ui.components;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

final class _TimeConversion {

    public static Date toDate(LocalDate value) {
        return toDate(value.atStartOfDay());
    }

    public static Date toDate(LocalDateTime value) {
        return new Date(toEpochMilli(value));
    }

    public static Date toDate(OffsetDateTime value) {
        return toDate(value.toLocalDateTime());
    }

    // -- HELPER

    private static final ZoneId zId = ZoneId.systemDefault();

    private static long toEpochMilli(LocalDateTime localDateTime){
        return localDateTime.atZone(zId).toInstant().toEpochMilli();
    }

}
