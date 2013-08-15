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

package org.apache.isis.core.runtime.logging;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FileSnapshotAppender extends SnapshotAppender {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FileSnapshotAppender.class);
    
    private String directoryPath;
    private String extension;
    private String fileName = "log-snapshot-";

    public FileSnapshotAppender(final org.apache.log4j.spi.TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public FileSnapshotAppender() {
        super();
    }

    public synchronized String getDirectory() {
        return directoryPath;
    }

    public synchronized String getExtension() {
        return extension;
    }

    public synchronized String getFileName() {
        return fileName;
    }

    public synchronized void setDirectory(final String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public synchronized void setExtension(final String extension) {
        this.extension = extension;
    }

    public synchronized void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected synchronized void writeSnapshot(final String message, final String details) {
        SnapshotWriter s;
        try {
            final String contentType = layout.getContentType();
            final String fileExtension = isEmpty(extension) ? contentType.substring(contentType.indexOf('/') + 1) : extension;
            s = new SnapshotWriter(directoryPath, fileName, fileExtension, message);
            s.appendLog(details);
            s.close();
        } catch (final FileNotFoundException e) {
            LOG.error("failed to open log file", e);
        } catch (final IOException e) {
            LOG.error("failed to write log file", e);
        }
    }

    private boolean isEmpty(final String extension2) {
        return extension2 == null || extension2.length() == 0;
    }
}
