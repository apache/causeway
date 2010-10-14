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


package org.apache.isis.extensions.dnd.calendar;

import java.util.Calendar;


public class MonthCells extends Cells {

    public MonthCells(Cells replacing) {
        super(replacing);
    }

    public int defaultColumns() {
        return 4;
    }

    public int defaultRows() {
        return 3;
    }

    public void roundDown() {
        date.set(Calendar.MONTH, 0);
    }

    public void add(int interval) {
        date.add(Calendar.MONTH, interval);
    }

    public String title(int cell) {
        Calendar d = (Calendar) date.clone();
        d.add(Calendar.MONTH, cell);
        String displayName = monthFormat.format(d.getTime()) + " " + d.get(Calendar.YEAR);
        return displayName;
    }

    protected int period(Calendar forDate) {
        return forDate.get(Calendar.YEAR) * 12 - forDate.get(Calendar.MONTH);
    }
}

