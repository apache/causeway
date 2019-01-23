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
import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Replacement for the use of System.out.println when adding temporary debug code, 
 * that needs to be removed later.
 * </p>
 * <p>EXAMPLE:<br/><pre>{@code 
 * _Probe probe = 
 *    _Probe.maxCallsThenExitWithStacktrace(1).label("IsisSessionFactoryDefault");
 * probe.println("Hallo World!");
 * }
 * </pre></p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0-M3
 */
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
    
    private final LongAdder counter = new LongAdder();
    
    private _Probe(long maxCalls, MaxCallsReachedAction maxAction) {
        this.maxCalls = maxCalls;
        this.maxAction = maxAction;
    }

    // -- FACTORIES
    
    public static _Probe maxCallsThenIgnore(long max) {
        return of(max, MaxCallsReachedAction.IGNORE);
    }

    public static _Probe maxCallsThenExit(long max) {
        return of(max, MaxCallsReachedAction.SYSTEM_EXIT);
    }

    public static _Probe maxCallsThenExitWithStacktrace(long max) {
        return of(max, MaxCallsReachedAction.SYSTEM_EXIT_WITH_STACKTRACE);
    }

    public static _Probe unlimited() {
        return of(Long.MAX_VALUE-1, MaxCallsReachedAction.IGNORE);
    }
    
    private static _Probe of(long maxCalls, MaxCallsReachedAction maxAction) {
        return new _Probe(maxCalls, maxAction);
    }
    
    // -- WITHERS
    
    public _Probe out(PrintStream out) {
        this.out = out;
        return this;
    }
    
    public _Probe label(String label) {
        this.label = label;
        return this;
    }

    public _Probe indentLiteral(String indentLiteral) {
        this.indentLiteral = indentLiteral;
        return this;
    }
    
    public _Probe emphasisFormat(String emphasisFormat) {
        this.emphasisFormat = emphasisFormat;
        return this;
    }
    
    // -- INDENTING
    
    public int currentIndent = 0;
    
    // -- PRINTING

    public void println(int indent, CharSequence chars) {
        if(counter.longValue()<maxCalls) {
            counter.increment();
            print_line(indent, chars);
            return;
        }
        
        switch (maxAction) {
        case IGNORE:
            return;
        case SYSTEM_EXIT:
            print_line(indent, chars);
            System.exit(0);
            return;
        case SYSTEM_EXIT_WITH_STACKTRACE:
            print_line(indent, chars);
            _Exceptions.dumpStackTrace(out, 0, 1000);
            System.exit(0);
            return;
        }

    }

    public void println(CharSequence chars) {
        println(currentIndent, chars);
    }

    public void println(int indent, String format, Object...args) {
        println(indent, String.format(format, args));
    }

    public void println(String format, Object...args) {
        println(currentIndent, format, args);
    }

    // -- CONVENIENT DEBUG TOOLS (STATIC)

    public static void sysOut(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    public static void errOut(String format, Object... args) {
        System.err.println(String.format(format, args));
    }

    // -- HELPER
    
    private void print_line(int indent, CharSequence chars) {
        final long counterValue = counter.longValue();
        for(int i=0; i<indent; ++i) {
            out.print(indentLiteral);
        }
        final String message = "["+label+" "+counterValue+"] "+chars; 
        out.println(String.format(emphasisFormat, message));
    }


}
