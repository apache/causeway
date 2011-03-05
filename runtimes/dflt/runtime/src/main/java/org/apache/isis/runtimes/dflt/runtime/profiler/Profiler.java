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


package org.apache.isis.runtimes.dflt.runtime.profiler;

import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;


public class Profiler {
    private final static String DELIMITER = "\t";
    private static NumberFormat floatFormat = NumberFormat.getNumberInstance(Locale.UK);
    private static NumberFormat integerFormat = NumberFormat.getNumberInstance(Locale.UK);
    private static int nextId = 0;
    private static int nextThread = 0;
    protected static ProfilerSystem profilerSystem = new ProfilerSystem();
    private static Hashtable threads = new Hashtable();

    public static String memoryLog() {
        final long free = memory();
        return integerFormat.format(free) + " bytes";
    }

    private static long memory() {
        return profilerSystem.memory();
    }

    private static long time() {
        return profilerSystem.time();
    }

    public static void setProfilerSystem(final ProfilerSystem profilerSystem) {
        Profiler.profilerSystem = profilerSystem;
    }

    private long elapsedTime = 0;
    private final int id;
    private long memory;
    private final String name;
    private long start = 0;
    private final String thread;
    private boolean timing = false;

    public Profiler(final String name) {
        this.name = name;
        synchronized (Profiler.class) {
            id = nextId++;
        }
        final Thread t = Thread.currentThread();
        final String thread = (String) threads.get(t);
        if (thread != null) {
            this.thread = thread;
        } else {
            this.thread = "t" + nextThread++;
            threads.put(t, this.thread);
        }
        memory = memory();
    }

    public long getElapsedTime() {
        return timing ? time() - start : elapsedTime;
    }

    public long getMemoryUsage() {
        return memory() - memory;
    }

    public String getName() {
        return name;
    }

    public String log() {
        return id + DELIMITER + thread + DELIMITER + getName() + DELIMITER + getMemoryUsage() + DELIMITER + getElapsedTime();
    }

    public void reset() {
        elapsedTime = 0;
        start = time();
        memory = memory();
    }

    public void start() {
        start = time();
        timing = true;
    }

    public void stop() {
        timing = false;
        final long end = time();
        elapsedTime += end - start;
    }

    public String memoryUsageLog() {
        return integerFormat.format(getMemoryUsage()) + " bytes";
    }

    public String timeLog() {
        return floatFormat.format(getElapsedTime() / 1000.0) + " secs";
    }

    @Override
    public String toString() {
        return getElapsedTime() + "ms - " + name;
    }

}
