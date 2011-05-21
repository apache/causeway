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

package org.apache.isis.runtimes.dflt.objectstores.nosql.file.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlStoreException;

public class LogWriter {

    private DataOutputStream writer;
    private boolean startNewFile = false;
    private int nextLogIdToWrite;

    public void startNewFile() {
        // don't start new file if old one is empty
        File file = Util.logFile(nextLogIdToWrite);
        if (file.exists() && file.length() > 0) {
            startNewFile = true;
        }
    }

    public synchronized void logNextSerialBatch(String name, long newBatchAt) {
        startNewFileIfNeeded();
        try {
            writer.write(("#transaction started - " + new Date().toString() + "\n").getBytes());
            writer.write('B');
            writer.write(name.getBytes(Util.ENCODING));
            writer.write(' ');
            writer.write(Long.toString(newBatchAt).getBytes(Util.ENCODING));
            writer.write('\n');
            writer.write('\n');
            writer.write("#transaction ended\n\n".getBytes());
            writer.flush();
        } catch (final IOException e) {
            throw new NoSqlStoreException("Failed to write serial number data to log file", e);
        }

    }

    public synchronized void logServiceEntry(String key, String name) {
        startNewFileIfNeeded();
        try {
            writer.write(("#transaction started - " + new Date().toString() + "\n").getBytes());
            writer.write('S');
            writer.write(key.getBytes(Util.ENCODING));
            writer.write(' ');
            writer.write(name.getBytes(Util.ENCODING));
            writer.write('\n');
            writer.write('\n');
            writer.write("#transaction ended\n\n".getBytes());
            writer.flush();
        } catch (final IOException e) {
            throw new NoSqlStoreException("Failed to write service entry data to log file", e);
        }
    }

    public synchronized void logWrites(final List<FileContent> items) {
        startNewFileIfNeeded();
        try {
            writer.write(("#transaction started - " + new Date().toString() + "\n").getBytes());
            for (final FileContent content : items) {
                writer.write(content.command);
                content.write(writer);
                writer.write('\n');
            }
            writer.write("#transaction ended\n\n".getBytes());
            writer.flush();
        } catch (final IOException e) {
            throw new NoSqlStoreException("Failed to write data to log file", e);
        }
    }

    private void startNewFileIfNeeded() {
        if (startNewFile) {
            close();
            openNewFile();
            startNewFile = false;
        }
    }

    private void openNewFile() {
        File file = findNextFile();
        openFile(file);
    }

    private void openFile(File file) {
        try {
            writer = new DataOutputStream(new FileOutputStream(file));
            startNewFile = false;
        } catch (final IOException e) {
            throw new NoSqlStoreException("Failed to open log file", e);
        }
    }

    private File findNextFile() {
        File file;
        do {
            nextLogIdToWrite++;
            file = Util.logFile(nextLogIdToWrite);
        } while (file.exists());
        return file;
    }

    public void startup() {
        findNextFile();
        nextLogIdToWrite--;
        startNewFile();
        if (!startNewFile) {
            File file = Util.logFile(nextLogIdToWrite);
            openFile(file);
        } else {
            openNewFile();
        }
    }

    public void shutdown() {
        close();
    }

    private void close() {
        try {
            writer.close();
        } catch (final IOException e) {
            throw new NoSqlStoreException("Falied to close log file", e);
        }
    }

    public synchronized boolean isWritten(long logId) {
        startNewFileIfNeeded();
        return logId < nextLogIdToWrite;
    }
}
