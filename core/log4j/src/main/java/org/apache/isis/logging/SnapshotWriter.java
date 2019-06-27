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

package org.apache.isis.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SnapshotWriter {
    private static final Format FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    private final PrintStream os;

    public SnapshotWriter(final String directoryPath, final String baseFileName, final String fileExtension, final String message) throws IOException {
        final File dir = new File(directoryPath == null || directoryPath.length() == 0 ? "." : directoryPath);
        if (!dir.exists()) {
            @SuppressWarnings("unused")
            final boolean created = dir.mkdirs();
        }

        final File indexFile = new File(dir, "index.txt");
        final Date date = new Date();
        final File logFile = new File(dir, baseFileName + FORMAT.format(date) + "." + fileExtension);

        try (RandomAccessFile index = new RandomAccessFile(indexFile, "rw")) {
            index.seek(index.length());
            index.writeBytes(logFile.getName() + ": " + message + "\n");
        }

        os = new PrintStream(new FileOutputStream(logFile));
    }

    public void appendLog(final String details) {
        os.println(details);
    }

    public void close() {
        if (os != null) {
            os.close();
        }
    }
}
