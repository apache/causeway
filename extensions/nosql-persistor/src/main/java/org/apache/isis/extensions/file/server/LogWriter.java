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


package org.apache.isis.extensions.file.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.isis.extensions.nosql.NoSqlStoreException;

public class LogWriter {
    
    private DataOutputStream writer;
    private boolean startNewFile = true;
    private int fileIndex;
    
    public void startNewFile() {
        startNewFile = true;
    }
    
    public synchronized void log(List<FileContent> items) {
        if (startNewFile) {
            close();
            openNewFile();
            startNewFile = false;
        }
        try {
            writer.write(("#transaction started - " + new Date().toString() + "\n").getBytes());
            for (FileContent content: items) {
                writer.write(content.command);
                writer.write(content.type.getBytes("utf-8"));
                writer.write(' ');
                writer.write(content.id.getBytes("utf-8"));
                writer.write(' ');
                writer.write(content.currentVersion.getBytes("utf-8"));
                writer.write(' ');
                writer.write(content.data.getBytes("utf-8"));
                writer.write('\n');
                writer.write('\n');
            }
            writer.write("#transaction ended\n\n".getBytes());
            writer.flush();
        } catch (IOException e) {
            throw new NoSqlStoreException("Failed to write data to log file", e);
        }
    }

    private void openNewFile() {
        File file;
        do {
              file = Util.logFile(fileIndex++);
        } while (file.exists());
        try {
            writer = new DataOutputStream(new FileOutputStream(file));
            startNewFile = false;
        } catch (IOException e) {
            throw new NoSqlStoreException("Failed to open log file", e);
        }
    }
    
    public void startup() {
        // TODO replay log.
        openNewFile();
    }

    public void shutdown() {
        close();
    }

    private void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new NoSqlStoreException("Falied to close log file", e);
        }
    }
}


