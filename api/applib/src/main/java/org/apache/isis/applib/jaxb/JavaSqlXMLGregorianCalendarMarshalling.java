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
package org.apache.isis.applib.jaxb;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JavaSqlXMLGregorianCalendarMarshalling {

    public static Timestamp toTimestamp(final XMLGregorianCalendar calendar) {
        return calendar != null
                ? new Timestamp(calendar.toGregorianCalendar().getTime().getTime())
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final Timestamp timestamp) {
        if(timestamp == null) {
            return null;
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(timestamp);
        return getDatatypeFactory().newXMLGregorianCalendar(c);
    }

    // this assumes DTF is thread-safe, which it most probably is..
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177
    static DatatypeFactory datatypeFactory = null;

    private static DatatypeFactory getDatatypeFactory() {
        if(datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
                return datatypeFactory;
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return datatypeFactory;
    }
}
