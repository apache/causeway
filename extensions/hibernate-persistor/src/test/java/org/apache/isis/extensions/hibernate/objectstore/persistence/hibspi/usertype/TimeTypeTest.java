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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.easymock.MockControl;
import org.hibernate.Hibernate;
import org.apache.isis.applib.value.Time;


public class TimeTypeTest extends TypesTestCase {
    public static TimeZone timeZone = TimeZone.getTimeZone("GMT");

    public void testNullSafeGetNotNull() throws Exception {
        final GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 05);
        final java.sql.Time returnedTime = new java.sql.Time(cal.getTimeInMillis());

        final MockControl<ResultSet> control = MockControl.createControl(ResultSet.class);
        final ResultSet rs = (ResultSet) control.getMock();
        control.expectAndReturn(rs.getTime(names[0]), returnedTime);
        control.expectAndReturn(rs.wasNull(), false);
        control.replay(); // finished recording

        final TimeType type = new TimeType();
        final Time returned = (Time) type.nullSafeGet(rs, names, null);
        assertEquals("hour", 18, returned.getHour());
        assertEquals("min", 5, returned.getMinute());

        control.verify();
    }

    public void testNullSafeGetIsNull() throws Exception {
        final MockControl<ResultSet> control = MockControl.createControl(ResultSet.class);
        final ResultSet rs = (ResultSet) control.getMock();
        control.expectAndReturn(rs.getTime(names[0]), null);
        control.expectAndReturn(rs.wasNull(), true);
        control.replay(); // finished recording

        final TimeType type = new TimeType();
        assertNull(type.nullSafeGet(rs, names, null));

        control.verify();
    }

    public void testNullSafeSetNotNull() throws Exception {
        final GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 05);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final java.sql.Time sqlTime = new java.sql.Time(cal.getTimeInMillis());
        final Time Time = new Time(sqlTime);

        final MockControl<PreparedStatement> control = MockControl.createControl(PreparedStatement.class);
        final PreparedStatement ps = (PreparedStatement) control.getMock();
        ps.setTime(1, sqlTime);
        control.replay(); // finished recording

        final TimeType type = new TimeType();
        type.nullSafeSet(ps, Time, 1);

        control.verify();
    }

    public void testNullSafeSetIsNull() throws Exception {
        final MockControl<PreparedStatement> control = MockControl.createControl(PreparedStatement.class);
        final PreparedStatement ps = (PreparedStatement) control.getMock();
        ps.setNull(1, Hibernate.TIME.sqlType());
        control.replay(); // finished recording

        final TimeType type = new TimeType();
        type.nullSafeSet(ps, null, 1);

        control.verify();
    }

    public void testBasics() {
        final TimeType type = new TimeType();
        super.basicTest(type);
        assertEquals("returned class", org.apache.isis.applib.value.Time.class, type.returnedClass());
    }

}
