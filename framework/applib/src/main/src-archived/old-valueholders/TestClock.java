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


package org.apache.isis.application.system;

import org.apache.isis.application.Clock;
import org.apache.isis.application.valueholder.Date;
import org.apache.isis.application.valueholder.DateTime;
import org.apache.isis.application.valueholder.Time;
import org.apache.isis.application.valueholder.TimeStamp;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class TestClock implements Clock {
    private static final TimeZone timeZone;
    static {
        timeZone = TimeZone.getTimeZone("GMT");
        Locale.setDefault(Locale.UK);
    }
    
    public TestClock() {
        Date.setClock(this);
        Time.setClock(this);
        DateTime.setClock(this);
        TimeStamp.setClock(this);
    }

    /**
     * Always return the time as 2003/8/17 21:30:25
     */
    public long getTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        
        c.set(Calendar.MILLISECOND, 0);

        c.set(Calendar.YEAR, 2003);
        c.set(Calendar.MONTH, 7);
        c.set(Calendar.DAY_OF_MONTH, 17);

        c.set(Calendar.HOUR_OF_DAY, 21);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 25);

        return c.getTime().getTime();
    }

}
