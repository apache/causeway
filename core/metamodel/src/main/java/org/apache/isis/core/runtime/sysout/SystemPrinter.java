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

package org.apache.isis.core.runtime.sysout;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.CloseableExtensions;
import org.apache.isis.core.runtime.about.AboutIsis;

public class SystemPrinter {

    private final PrintStream output;

    public SystemPrinter() {
        this(System.out);
    }

    public SystemPrinter(final PrintStream output) {
        this.output = output;
    }

    protected PrintStream getOutput() {
        return output;
    }

    void print(final String string) {
        output.println(string);
    }

    void printBlock(final String title) {
        print("");
        print("------------------------------------------");
        print(title);
        print("------------------------------------------");
    }

    public void printDiagnostics() {
        print("------- Apache Isis diagnostics report -------");
        printVersion();

        printBlock("System properties");
        final Properties properties = System.getProperties();
        final Enumeration<?> propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String name = (String) propertyNames.nextElement();
            final String property = properties.getProperty(name);
            final StringBuilder buf = new StringBuilder();
            if (name.endsWith(".path") || name.endsWith(".dirs")) {
                final String[] split = property.split(":");
                buf.append(split[0]);
                for (int i = 1; i < split.length; i++) {
                    buf.append("\n\t\t" + split[i]);
                }
            }
            print(name + "= " + buf.toString());
        }

        File file = new File("../lib");
        if (file.isDirectory()) {
            final String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return name.endsWith(".jar");
                }
            });
            printBlock("Libs");
            for (final String file2 : files) {
                print(file2);
            }
        }

        printBlock("Locale information");
        print("Default locale: " + Locale.getDefault());
        print("Default timezone: " + TimeZone.getDefault());

        file = new File("config");
        if (file.isDirectory()) {
            final String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    return new File(dir, name).isFile();
                }
            });
            printBlock("Config files");
            for (final String file2 : files) {
                print(file2);
            }

            for (final String file2 : files) {
                print("");
                print("--------------------------------------------------------------------------------------------------------");
                print(file2);
                print("");
                LineNumberReader fileInputStream = null;
                try {
                    fileInputStream = new LineNumberReader(new FileReader(new File(file, file2)));
                    String line;
                    while ((line = fileInputStream.readLine()) != null) {
                        print(fileInputStream.getLineNumber() + "  " + line);
                    }
                } catch (final Exception e) {
                    throw new IsisException(e);
                } finally {
                    CloseableExtensions.closeSafely(fileInputStream);
                }
                print("");
            }

        }
    }

    public void printVersion() {
        final String date = AboutIsis.getFrameworkCompileDate();
        final String compileDate = date == null ? "" : ", compiled on " + date;
        print(AboutIsis.getFrameworkName() + ", " + AboutIsis.getFrameworkVersion() + compileDate);
    }

    public void printErrorMessage(final String message) {
        output.println("Error: " + message);
    }

}
