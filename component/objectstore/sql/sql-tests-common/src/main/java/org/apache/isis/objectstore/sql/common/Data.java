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
package org.apache.isis.objectstore.sql.common;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;

public class Data {

    // private static final TimeZone GMTm2_TIME_ZONE;

    // Helper values
    static final java.sql.Date sqlDate;
    static final java.sql.Date sqlDate20100305;

    static {
        /*
         * 
         * // For testing -ve offset timezone local regions. GMTm2_TIME_ZONE = TimeZone.getTimeZone("GMT-0200");
         * //GMTm2_TIME_ZONE = TimeZone.getTimeZone("UTC"); TimeZone.setDefault(GMTm2_TIME_ZONE);
         */

        /*
         * TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC"); if (timeZone == null) { timeZone =
         * TimeZone.getTimeZone("UTC"); } UTC_TIME_ZONE = timeZone;
         */

        /*
         * There is still an issue assigning a java.sql.Date variable from a calendar. final Calendar cal =
         * Calendar.getInstance(); cal.setTimeZone(UTC_TIME_ZONE); cal.clear(); cal.set(Calendar.YEAR, 2011);
         * cal.set(Calendar.MONTH, 4-1); cal.set(Calendar.DAY_OF_MONTH, 8);
         */
        // 2011-4-8 = 1,270,684,800,000
        final Date date20100308 = new Date(2010, 4, 8);
        sqlDate = new java.sql.Date(date20100308.getMillisSinceEpoch());

        sqlDate20100305 = new java.sql.Date(new Date(2010, 3, 5).getMillisSinceEpoch());
    }

    static final Date applibDate = new Date(2010, 3, 5); // 2010-03-05 =
                                                         // 1,267,747,200,000
    static final DateTime dateTime = new DateTime(2010, 3, 5, 1, 23); // 1,267,752,180,000
    static final TimeStamp timeStamp = new TimeStamp(dateTime.millisSinceEpoch());
    static final Time time = new Time(14, 56); // 53,760,000

    static final Color color = Color.WHITE;
    static final Image image = new Image(new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } });
    static final Password password = new Password("password");
    static final Percentage percentage = new Percentage(42);
    static final Money money = new Money(99.99, "ZAR");

    // Standard values
    static final int intMaxValue = Integer.MAX_VALUE;
    static final short shortMaxValue = Short.MAX_VALUE;
    static final long longMaxValue = Long.MAX_VALUE;
    static final double doubleMaxValue = 1e308;// Double.MAX_VALUE;
    static final float floatMaxValue = (float) 1e37;// Float.MAX_VALUE;

    static final int intMinValue = Integer.MIN_VALUE;
    static final short shortMinValue = Short.MIN_VALUE;
    static final long longMinValue = Long.MIN_VALUE;
    static final double doubleMinValue = 1e-307;// Double.MIN_VALUE;
    static final float floatMinValue = (float) 1e-37;// Float.MIN_VALUE;

    // Collection mapper tests
    static final List<String> stringList1 = Arrays.asList("Baking", "Bakery", "Canned", "Dairy");
    static final List<String> stringList2 = Arrays.asList("Fridge", "Deli", "Fresh Produce", "Frozen", "Household",
        "Other..");

    public static List<String> getTableNames() {
        return Arrays.asList("sqldataclass", "simpleclass", "simpleclasstwo", "primitivevaluedentity");
    }

}
