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

public class WeekCells extends Cells {

    public WeekCells(final Cells replacing) {
        super(replacing);
    }

    @Override
    public int defaultColumns() {
        return 4;
    }

    @Override
    public int defaultRows() {
        return 3;
    }

    @Override
    public void add(final int interval) {
        add(date, interval);
    }

    public void add(final Calendar d, final int interval) {
        d.add(Calendar.DAY_OF_MONTH, 7 * interval);
    }

    @Override
    public String title(final int cell) {
        final Calendar d = (Calendar) date.clone();
        add(d, cell);
        final String displayName = d.get(Calendar.DAY_OF_MONTH) + " " + monthFormat.format(d.getTime());
        return "w/b " + displayName;
    }

    @Override
    protected int period(final Calendar forDate) {
        return forDate.get(Calendar.YEAR) * 12 - forDate.get(Calendar.WEEK_OF_YEAR);
    }
}
