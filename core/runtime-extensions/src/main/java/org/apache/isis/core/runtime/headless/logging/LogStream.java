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
package org.apache.isis.core.runtime.headless.logging;

import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LogStream extends OutputStream {

    private final LeveledLogger logger;

    private final StringBuilder buf = new StringBuilder();

    public static PrintStream logPrintStream(final Logger logger, final Level level) {
        return new PrintStream(new LogStream(logger, level));
    }

    public LogStream(final Logger logger, final Level level) {
        this.logger = new LeveledLogger(logger, level);
    }

    @Override
    public void close() {}

    @Override
    public void flush() {
        logger.log(toString());

        // Clear the buffer
        buf.delete(0, buf.length());
    }

    @Override
    public void write(byte[] b) {
        String str = new String(b);
        this.buf.append(str);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String str = new String(b, off, len);
        this.buf.append(str);
    }

    @Override
    public void write(int codePoint) {
        char[] chars = Character.toChars(codePoint);
        this.buf.append(chars);
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
