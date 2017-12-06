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
package org.apache.isis.applib.fixturescripts.clock;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.clock.TickingFixtureClock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScriptWithExecutionStrategy;
import org.apache.isis.applib.fixturescripts.FixtureScripts;


public class TickingClockFixture extends FixtureScript implements FixtureScriptWithExecutionStrategy {

    @Override
    protected void execute(ExecutionContext executionContext) {

        final Clock instance = Clock.getInstance();

        if(instance instanceof TickingFixtureClock) {
            TickingFixtureClock.reinstateExisting();
            executionContext.executeChild(this, ClockFixture.setTo("2014-05-18"));
            TickingFixtureClock.replaceExisting();
        }

        if(instance instanceof FixtureClock) {
            executionContext.executeChild(this, ClockFixture.setTo("2014-05-18"));
        }
    }

    @Override
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_CLASS;
    }
}
