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
package org.apache.causeway.commons.internal.base;

import java.io.Serializable;
import java.util.Locale;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Time measuring utilities.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Timing {

    private _Timing(){}

    /**
     * @return a new 'now' started instance of {@link StopWatch}
     */
    public static StopWatch now() {
        return new StopWatch();
    }

    /**
     *
     * @param startedAtSystemNanos
     * @return a new {@code startedAtSystemNanos} instance of {@link StopWatch}
     */
    public static StopWatch atSystemNanos(final long startedAtSystemNanos) {
        return new StopWatch(startedAtSystemNanos);
    }

    /**
     * Non thread safe start/stop watch utilizing the currently running
     * JVM's high-resolution time source.
     * @implNote using {@link System#nanoTime()} as this is best suited to measure elapsed time
     */
    public static final class StopWatch implements Serializable {
        private static final long serialVersionUID = 1L;

        private long t0 = 0;
        private long t1 = 0;
        @Getter private boolean stopped;

        private StopWatch(final long startedAtSystemNanos) {
            t0 = startedAtSystemNanos;
        }

        private StopWatch() {
            start();
        }

        /** On repeated calls simply restarts the clock. */
        public StopWatch start() {
            t0 = System.nanoTime();
            stopped = false;
            return this;
        }

        /** Snapshots a split time.
         * Repeated calls will update the measurement value leaving the starting point unchanged. */
        public StopWatch stop() {
            t1 = System.nanoTime();
            stopped  = true;
            return this;
        }

        public double getSeconds() {
            return 0.001 * getMillis();
        }

        /**
         * @return elapsed nano seconds since started
         * (or when stopped, the time interval between started and stopped)
         */
        public long getNanos() {
            return stopped ? t1 - t0 : System.nanoTime() - t0 ;
        }

        /**
         * @return elapsed micro seconds since started
         * (or when stopped, the time interval between started and stopped)
         */
        public long getMicros() {
            return getNanos()/1000L;
        }

        /**
         * @return elapsed milli seconds since started
         * (or when stopped, the time interval between started and stopped)
         */
        public long getMillis() {
            return getNanos()/1000_000L;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%d ms", getMillis());
        }

    }

    public static StopWatch run(final Runnable runnable) {
        final StopWatch watch = now();
        runnable.run();
        return watch.stop();
    }

    public static void runVerbose(final String label, final Runnable runnable) {
        System.out.println(String.format(Locale.US, "Entering call '%s'", label));
        final StopWatch watch = run(runnable);
        System.out.println(String.format(Locale.US, "Running '%s' took %d ms", label, watch.getMillis()));
    }

    public static <T> T callVerbose(final String label, final Supplier<T> callable) {
        System.out.println(String.format(Locale.US, "Entering call '%s'", label));
        final StopWatch watch = now();
        T result = callable.get();
        watch.stop();
        System.out.println(String.format(Locale.US, "Calling '%s' took %d ms", label, watch.getMillis()));
        return result;
    }

    public static void runVerbose(final Logger log, final String label, final Runnable runnable) {
        final StopWatch watch = run(runnable);
        log.info(String.format(Locale.US, "Running '%s' took %d ms", label, watch.getMillis()));
    }

    public static <T> T callVerbose(final Logger log, final String label, final Supplier<T> callable) {
        final StopWatch watch = now();
        T result = callable.get();
        watch.stop();
        log.info(String.format(Locale.US, "Calling '%s' took %d ms", label, watch.getMillis()));
        return result;
    }

}
