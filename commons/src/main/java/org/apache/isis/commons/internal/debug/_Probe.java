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
package org.apache.isis.commons.internal.debug;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Replacement for the use of System.out.println when adding temporary debug code,
 * that needs to be removed later.
 * </p>
 * <p>EXAMPLE:<br/><pre>{@code
 * _Probe probe =
 *    _Probe.maxCallsThenExitWithStacktrace(1).label("IsisInteractionFactoryDefault");
 * probe.println("Hallo World!");
 * }
 * </pre></p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
@Log4j2
public class _Probe {

    public static enum MaxCallsReachedAction {
        IGNORE,
        SYSTEM_EXIT,
        SYSTEM_EXIT_WITH_STACKTRACE
    }

    private final long maxCalls;
    private final MaxCallsReachedAction maxAction;
    private PrintStream out = System.out;
    private String label = "Probe";
    private String indentLiteral = "  ";
    private String emphasisFormat = "__PROBE__ %s";
    private boolean silenced = false;

    private final LongAdder counter = new LongAdder();
    private final LongAdder nanoCounter = new LongAdder();

    private _Probe(final long maxCalls, final MaxCallsReachedAction maxAction) {
        this.maxCalls = maxCalls;
        this.maxAction = maxAction;
    }

    // -- FACTORIES

    public static _Probe maxCallsThenIgnore(final long max) {
        return of(max, MaxCallsReachedAction.IGNORE);
    }

    public static _Probe maxCallsThenExit(final long max) {
        return of(max, MaxCallsReachedAction.SYSTEM_EXIT);
    }

    public static _Probe maxCallsThenExitWithStacktrace(final long max) {
        return of(max, MaxCallsReachedAction.SYSTEM_EXIT_WITH_STACKTRACE);
    }

    public static _Probe unlimited() {
        return of(Long.MAX_VALUE-1, MaxCallsReachedAction.IGNORE);
    }

    private static _Probe of(final long maxCalls, final MaxCallsReachedAction maxAction) {
        return new _Probe(maxCalls, maxAction);
    }

    // -- WITHERS

    public _Probe out(final PrintStream out) {
        this.out = out;
        return this;
    }

    public _Probe label(final String label) {
        this.label = label;
        return this;
    }

    public _Probe indentLiteral(final String indentLiteral) {
        this.indentLiteral = indentLiteral;
        return this;
    }

    public _Probe emphasisFormat(final String emphasisFormat) {
        this.emphasisFormat = emphasisFormat;
        return this;
    }

    public _Probe silence() {
        this.silenced = true;
        return this;
    }

    // -- INDENTING

    private int currentIndent = 0;

    // -- PRINTING

    public void println(final int indent, final CharSequence chars) {
        if(counter.longValue()<maxCalls) {
            counter.increment();
            if(!silenced) {
                print_line(indent, chars);
            }
            return;
        }

        switch (maxAction) {
        case IGNORE:
            return;
        case SYSTEM_EXIT:
            counter.increment();
            print_line(indent, chars);
            System.exit(0);
            return;
        case SYSTEM_EXIT_WITH_STACKTRACE:
            counter.increment();
            print_line(indent, chars);
            _Exceptions.dumpStackTrace(out, 0, 1000);
            System.exit(0);
            return;
        }

    }

    public void println(final CharSequence chars) {
        println(currentIndent, chars);
    }

    public void println(final int indent, final String format, final Object...args) {
        println(indent, String.format(format, args));
    }

    public void println(final String format, final Object...args) {
        println(currentIndent, format, args);
    }

    public void warnNotImplementedYet(final String format, final Object... args) {
        val warnMsg = String.format(format, args);
        val restore_out = out;
        out=System.err;
        println("WARN NotImplementedYet %s", warnMsg);
        errOut("-------------------------------------");
        _Exceptions.dumpStackTrace(System.err, 1, 12);
        errOut("-------------------------------------");
        out=restore_out;
    }

    public void run(final Runnable runnable) {
        val t0 = System.nanoTime();
        runnable.run();
        val nanos = System.nanoTime() - t0;
        nanoCounter.add(nanos);
        println("total runtime %d ms", nanoCounter.longValue()/1000_000);
    }

    // -- DEBUG ENTRY POINTS

    public static enum EntryPoint {
        USER_INTERACTION
    }

    /** idea is to keep these for reuse (so these are not just for temporary troubleshooting) */
    public static void entryPoint(final EntryPoint entryPoint, final String description) {
        if(log.isDebugEnabled()) {
            log.debug("entering {}: {}", entryPoint.name(), description);
        }
    }

    // -- CONVENIENT DEBUG TOOLS (STATIC)

    public static String currentThreadId() {
        val ct = Thread.currentThread();
        return String.format("Thread[%s (%d)])", ct.getName(), ct.getId());
    }

    public static void sysOut(final String format, final Object... args) {
        System.out.println(String.format(format, args));
    }

    public static void errOut(final String format, final Object... args) {
        System.err.println(String.format(format, args));
    }

    private static final Map<String, String> abbreviations =
            Map.of(
                    "org.apache.isis", "~",
                    "core", "c",
                    "applib", "alib",
                    "metamodel", "mm",
                    "runtime", "rt",
                    "viewer", "vw"
                    );
    public static String compact(final Class<?> cls) {
        String[] name = {cls.getName()};
        // pre-process for isis
        abbreviations.forEach((k, v)->{
            if(name[0].startsWith(k)) {
                name[0] = v + name[0].substring(k.length());
            }
        });
        return _Strings.splitThenStream(name[0], ".")
                .map(part->_With.mapIfPresentElse(abbreviations.get(part), value->value, part))
                .collect(Collectors.joining("."));
    }

    // -- HELPER

    private void print_line(final int indent, final CharSequence chars) {
        final long counterValue = counter.longValue();
        for(int i=0; i<indent; ++i) {
            out.print(indentLiteral);
        }
        final String message = "["+label+" "+counterValue+"] "+chars;
        out.println(String.format(emphasisFormat, message));
    }

}
