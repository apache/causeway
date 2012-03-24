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
package org.apache.isis.viewer.bdd.common.story.bootstrapping;

import java.util.Calendar;
import java.util.Date;

import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.viewer.bdd.common.Scenario;

public class SetClock extends AbstractHelper {

    public SetClock(final Scenario story) {
        super(story);
    }

    public void setClock(final Date date) {
        final FixtureClock clock = FixtureClock.initialize();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        clock.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        clock.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

}
