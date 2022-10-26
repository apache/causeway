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
package org.apache.causeway.testing.unittestsupport.applib.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 * @since 2.0 {@index}
 */
public class NullPrintStream extends PrintStream {
    public NullPrintStream() {
        super(new NullByteArrayOutputStream());
    }

    private static class NullByteArrayOutputStream extends ByteArrayOutputStream {
        @Override
        public synchronized void write(int b) {
            // do nothing
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            // do nothing
        }

        @Override
        public synchronized void writeTo(OutputStream out) throws IOException {
            // do nothing
        }
    }

    public static void suppressingStdout(Runnable r)  {
        suppressing(r, true, false);
    }

    public static <T> T suppressingStdout(Callable<T> c) throws Exception {
        return suppressing(c, true, false);
    }

    public static void suppressingStderr(Runnable r) {
        suppressing(r, false, true);
    }

    public static <T> T suppressingStderr(Callable<T> c) throws Exception {
        return suppressing(c, false, true);
    }

    public static void suppressing(Runnable r) throws Exception {
        suppressing(r, true, true);
    }

    public static <T> T suppressing(Callable<T> c) throws Exception {
        return suppressing(c, true, true);
    }

    private static void suppressing(Runnable r, boolean suppressStdout, boolean suppressStderr) {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        try {
            if (suppressStdout) {
                System.setOut(new NullPrintStream());
            }
            if (suppressStderr) {
                System.setErr(new NullPrintStream());
            }
            r.run();
        } finally {
            if (suppressStdout) {
                System.setOut(stdout);
            }
            if (suppressStdout) {
                System.setErr(stderr);
            }
        }
    }

    private static <T> T suppressing(Callable<T> c, boolean suppressStdout, boolean suppressStderr) throws Exception {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        try {
            if (suppressStdout) {
                System.setOut(new NullPrintStream());
            }
            if (suppressStderr) {
                System.setErr(new NullPrintStream());
            }
            return c.call();
        } finally {
            if (suppressStdout) {
                System.setOut(stdout);
            }
            if (suppressStdout) {
                System.setErr(stderr);
            }
        }
    }

}
