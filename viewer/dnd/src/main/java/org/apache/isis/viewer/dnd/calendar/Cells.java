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

package org.apache.isis.viewer.dnd.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class Cells {
    protected final DateFormat monthFormat = new SimpleDateFormat("MMM");
    protected final DateFormat dayFormat = new SimpleDateFormat("EEE");
    protected Calendar date;

    public Cells(final Cells replacing) {
        if (replacing == null) {
            today();
        } else {
            date = replacing.date;
        }
    }

    public void today() {
        date = Calendar.getInstance();
        roundDown();
    }

    public final void setDate(final Calendar date) {
        this.date = date;
    }

    public void roundDown() {
    }

    abstract int defaultRows();

    abstract int defaultColumns();

    abstract void add(int interval);

    abstract String title(int cell);

    public String header(final int cell) {
        return null;
    }

    public int getPeriodFor(final Date date) {
        final Calendar forDate = Calendar.getInstance();
        forDate.setTime(date);
        final int baseline = period(this.date);
        final int comparativePeriod = period(forDate);
        return baseline - comparativePeriod;
    }

    protected abstract int period(Calendar forDate);

}
