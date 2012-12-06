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

package org.apache.isis.core.runtime.optionhandler;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import org.apache.isis.core.runtime.sysout.SystemPrinter;

public class BootPrinter extends SystemPrinter {

    private final PrintWriter printWriter;
    private final String className;

    public BootPrinter(final Class<?> cls, final PrintStream output) {
        super(output);
        this.printWriter = new PrintWriter(getOutput());
        className = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
    }

    public BootPrinter(final Class<?> cls) {
        this(cls, System.out);
    }

    public void printErrorAndHelp(final Options options, final String formatStr, final Object... args) {
        getOutput().println(String.format(formatStr, args));
        printHelp(options);
        printWriter.flush();
    }

    public void printHelp(final Options options) {
        final HelpFormatter help = new HelpFormatter();
        help.printHelp(printWriter, 80, className + " [options]", null, options, 0, 0, null, false);
        printWriter.flush();
    }

}
