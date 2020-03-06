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
package org.apache.isis.subdomains.base.applib.utils;

import org.hamcrest.core.Is;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

public class JodaPeriodUtilsTest {

    public static class AsPeriod extends JodaPeriodUtilsTest {

        @Test
        public void happyCase() {
            Period period = JodaPeriodUtils.asPeriod("6y6m3d");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2006, 7, 4)));
        }

        @Test
        public void withSpaces() {
            Period period = JodaPeriodUtils.asPeriod("  6Y  6m  ");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2006, 7, 1)));
        }

        @Test
        public void whenMalformed() {
            Period period = JodaPeriodUtils.asPeriod("6x6y");
            LocalDate startDate = new LocalDate(2000, 1, 1);
            Assert.assertThat(startDate.plus(period), Is.is(new LocalDate(2000, 1, 1)));
        }
    }

    public static class AsString extends JodaPeriodUtilsTest {
        @Test
        public void happyCase() throws Exception {
            Period period = new Period(new LocalDate(2000, 1, 1), new LocalDate(2006, 7, 2));
            Assert.assertThat(JodaPeriodUtils.asString(period), Is.is("6 years, 6 months & 1 day"));
        }
    }

    public static class AsSimpleString extends JodaPeriodUtilsTest {
        @Test
        public void happyCase() throws Exception {
            Period period = new Period(new LocalDate(2000, 1, 1), new LocalDate(2006, 7, 2));
            Assert.assertThat(JodaPeriodUtils.asSimpleString(period), Is.is("6y6m1d"));
        }
    }

}
