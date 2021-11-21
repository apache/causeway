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
package org.apache.isis.core.metamodel.facets.value;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalTimeValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlTimeValueSemantics;

import static org.junit.Assert.assertEquals;

public class JavaSqlTimeValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase {

    private Time twoOClock;
    private JavaSqlTimeValueSemantics valueSemantics;

    @Before
    public void setUpObjects() throws Exception {

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));

        c.set(Calendar.MILLISECOND, 0);

        c.set(Calendar.YEAR, 0);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 0);

        c.set(Calendar.HOUR_OF_DAY, 14);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        twoOClock = new Time(c.getTimeInMillis());

        ValueSemanticsAbstract<LocalTime> delegate =
                new LocalTimeValueSemantics();

        setSemantics(valueSemantics = new JavaSqlTimeValueSemantics() {
            @Override
            public ValueSemanticsAbstract<LocalTime> getDelegate() {
                return delegate;
            }
        });

    }

    @Ignore // flaky
    @Test
    public void testNewTime() {
        final String asEncodedString = valueSemantics.toEncodedString(twoOClock);
        assertEquals("140000000", asEncodedString);
    }

//    @Ignore // flaky
//    @Test
//    public void testAdd() {
//        final Object newValue = value.add(twoOClock, 0, 0, 0, 1, 15);
//        assertEquals("15:15:00", newValue.toString());
//    }
//
//    @Ignore // flaky
//    @Test
//    public void testAdd2() {
//        final Object newValue = value.add(twoOClock, 0, 0, 0, 0, 0);
//        assertEquals("14:00:00", newValue.toString());
//    }
}
