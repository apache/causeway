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

package org.apache.isis.core.runtime.profiler;

import junit.framework.TestCase;

public class ProfilerTest extends TestCase {
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(ProfilerTest.class);
    }

    private Profiler profiler;

    @Override
    public void setUp() {
        Profiler.setProfilerSystem(new ProfilerTestSystem());
        profiler = new Profiler("name");
    }

    public void testFreeMemory() {
        assertEquals("20,300 bytes", Profiler.memoryLog());
    }

    public void testMemoryUsage() {
        assertEquals(10300, profiler.getMemoryUsage());
        assertEquals(20000, profiler.getMemoryUsage());
    }

    public void testMemoryUsageLog() {
        assertEquals("10,300 bytes", profiler.memoryUsageLog());
    }

    public void testTiming() {
        profiler.start();
        profiler.stop();
        assertEquals(100, profiler.getElapsedTime());
    }

    public void testTimingLog() {
        profiler.start();
        profiler.stop();
        assertEquals("0.1 secs", profiler.timeLog());
    }

    public void testContinueWithStartAndStopPausesTiming() {
        profiler.start();
        profiler.stop();

        profiler.start();
        profiler.stop();
        assertEquals(400, profiler.getElapsedTime());
    }

    public void testResetDuringTiming() {
        profiler.start();

        profiler.reset();
        assertEquals(200, profiler.getElapsedTime());
    }

    public void testResetAfterStopResetsToZero() {
        profiler.start();
        profiler.stop();

        profiler.reset();
        assertEquals(0, profiler.getElapsedTime());

        profiler.start();
        profiler.stop();
        assertEquals(400, profiler.getElapsedTime());
    }

    public void testZero() {
        assertEquals(0, profiler.getElapsedTime());
    }

    public void testRepeatedElapseTimeAfterStopGivesSameTime() {
        profiler.start();
        profiler.stop();
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(100, profiler.getElapsedTime());
    }

    public void testRepeatedElapseTimeGivesLatestTime() {
        profiler.start();
        assertEquals(100, profiler.getElapsedTime());
        assertEquals(300, profiler.getElapsedTime());
        assertEquals(600, profiler.getElapsedTime());
    }

}
