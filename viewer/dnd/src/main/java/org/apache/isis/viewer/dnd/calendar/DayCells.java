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

import java.util.Calendar;

public class DayCells extends Cells {

    public DayCells(final Cells replacing) {
        super(replacing);
    }

    @Override
    public int defaultColumns() {
        return 7;
    }

    @Override
    public int defaultRows() {
        return 2;
    }

    @Override
    public void add(final int interval) {
        date.add(Calendar.DAY_OF_WEEK, interval);
    }

    @Override
    public void roundDown() {
        final int offset = date.get(Calendar.DAY_OF_WEEK) - date.getFirstDayOfWeek();
        date.add(Calendar.DAY_OF_MONTH, -offset);
    }

    @Override
    public String title(final int cell) {
        final Calendar d = (Calendar) date.clone();
        d.add(Calendar.DAY_OF_WEEK, cell);
        final String displayName = dayFormat.format(d.getTime()) + " " + d.get(Calendar.DAY_OF_MONTH) + " " + monthFormat.format(d.getTime());
        return displayName;
    }

    @Override
    public String header(final int cell) {
        final Calendar d = (Calendar) date.clone();
        d.add(Calendar.DAY_OF_WEEK, cell);
        return dayFormat.format(d.getTime());
    }

    @Override
    protected int period(final Calendar forDate) {
        return forDate.get(Calendar.YEAR) * 12 - forDate.get(Calendar.DAY_OF_YEAR);
    }

}
