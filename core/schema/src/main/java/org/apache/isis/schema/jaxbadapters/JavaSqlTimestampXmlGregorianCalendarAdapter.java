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
package org.apache.isis.schema.jaxbadapters;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Note: not actually registered as a JAXB adapter.
 */
public class JavaSqlTimestampXmlGregorianCalendarAdapter  {


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

    public static java.sql.Timestamp parse(final XMLGregorianCalendar calendar) {
        return calendar != null
                ? new Timestamp(calendar.toGregorianCalendar().getTime().getTime())
                : null;
    }

    public static XMLGregorianCalendar print(final java.sql.Timestamp timestamp) {
        if(timestamp == null) {
            return null;
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(timestamp);
        return getDatatypeFactory().newXMLGregorianCalendar(c);
    }

    public static class ForJaxb extends XmlAdapter<XMLGregorianCalendar, java.sql.Timestamp> {

        @Override
        public java.sql.Timestamp unmarshal(final XMLGregorianCalendar timestampStr) throws Exception {
            return JavaSqlTimestampXmlGregorianCalendarAdapter.parse(timestampStr);
        }

        @Override
        public XMLGregorianCalendar marshal(final java.sql.Timestamp timestamp) throws Exception {
            return JavaSqlTimestampXmlGregorianCalendarAdapter.print(timestamp);
        }
    }

}
