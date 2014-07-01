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

package org.apache.isis.objectstore.nosql.db.file.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.objectstore.nosql.NoSqlStoreException;

public class FileServerProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FileServerProcessor.class);

    private boolean acceptNewRequests = true;
    private LockManager locks;
    private LogWriter logger;

    public void startup() {
        Util.ensureDirectoryExists();
        logger = new LogWriter();
        logger.startup();
        locks = new LockManager();
    }

    public void shutdown() {
        acceptNewRequests = false;
        locks.waitUntilAllRealeased();
        logger.shutdown();
    }

    LogWriter getLogger() {
        return logger;
    }

    public void process(final ServerConnection connection) {
        try {
            if (acceptNewRequests) {
                connection.readCommand();
                final char command = connection.getCommand();
                switch (command) {
                case 'L':
                    list(connection);
                    break;

                case 'R':
                    read(connection);
                    break;

                case 'W':
                    write(connection);
                    break;

                case 'I':
                    hasInstances(connection);
                    break;

                case 'S':
                    service(connection);
                    break;

                case 'T':
                    saveService(connection);
                    break;

                case 'N':
                    nextSerialBatch(connection);
                    break;

                case 'X':
                    status(connection);
                    break;

                default:
                    LOG.warn("Unrecognised command " + command);
                    connection.error("Unrecognised command " + command);
                }
            } else {
                connection.abort();
            }
        } catch (final Exception e) {
            LOG.error("Request failed", e);
            connection.error("Remote exception thrown:\n" + e.getMessage(), e);

        } finally {
            connection.close();
        }
    }

    private void list(final ServerConnection connection) {
        try {
            connection.endCommand();
            final String type = connection.getRequest();
            int limit = connection.getRequestAsInt();
            if (limit == 0) {
                limit = Integer.MAX_VALUE;
            }

            final File[] listFiles = listFiles(type);
            if (listFiles != null) {
                connection.ok();
                for (final File file : listFiles) {
                    final String fileName = file.getName();
                    final String id = fileName.substring(0, fileName.length() - 5);
                    final DataFileReader reader = findInstance(type, id, connection);
                    readInstance(reader, connection);
                    locks.release(id, getTransactionId());
                    if (limit-- < 0) {
                        break;
                    }
                }
                connection.endBlock();
            } else {
                connection.response("");
            }

        } catch (final IOException e) {
            throw new NoSqlStoreException(Util.READ_ERROR, e);
        }
    }

    private File[] listFiles(final String type) {
        final File[] listFiles = Util.directory(type).listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().endsWith(".data");
            }
        });
        return listFiles;
    }

    private void read(final ServerConnection connection) {
        String type = null;
        String id = null;
        try {
            connection.endCommand();
            type = connection.getRequest();
            id = connection.getRequest();
            final DataFileReader reader = findInstance(type, id, connection);
            if (reader == null) {
                connection.notFound(Util.FILE_NOT_FOUND + " for " + type + "/" + id);
            } else {
                connection.ok();
                readInstance(reader, connection);
            }
        } catch (final IOException e) {
            throw new NoSqlStoreException(Util.READ_ERROR + " for " + type + "/" + id, e);
        } finally {
            locks.release(id, getTransactionId());
        }

    }

    private DataFileReader findInstance(final String type, final String id, final ServerConnection connection) throws IOException {
        LOG.debug("reading file " + id);
        locks.acquireRead(id, getTransactionId());
        try {
            return new DataFileReader(type, id);
        } catch (final FileNotFoundException e) {
            LOG.error(Util.FILE_NOT_FOUND + " for " + type + "/" + id, e);
            return null;
        }
    }

    private void readInstance(final DataFileReader reader, final ServerConnection connection) {
        final String data = reader.getData();
        reader.close();
        connection.responseData(data);
    }

    private void write(final ServerConnection connection) {
        List<FileContent> files = null;
        try {
            files = getWriteRequests(connection);
            final String error = acquireLocks(files);
            if (error == null) {
                logger.logWrites(files);
                final DataFileWriter content = new DataFileWriter(files);
                content.writeData();
                connection.ok();
            } else {
                connection.error(error);
            }

        } catch (final IOException e) {
            throw new NoSqlStoreException("Failed to write data", e);
        } finally {
            if (files != null) {
                releaseLocks(files);
            }
        }
    }

    private List<FileContent> getWriteRequests(final ServerConnection connection) throws IOException {
        final ArrayList<FileContent> files = new ArrayList<FileContent>();
        while (connection.readWriteHeaders()) {
            final char command = connection.getCommand();
            final String type = connection.getRequest();
            final String id = connection.getRequest();
            final String currentVersion = connection.getRequest();
            final String newVersion = connection.getRequest();
            LOG.debug("write for " + type + "@" + id + " v." + newVersion);

            final String buf = connection.getData();
            files.add(new FileContent(command, id, currentVersion, newVersion, type, buf));
        }
        // connection.endCommand();
        return files;
    }

    private String acquireLocks(final List<FileContent> list) throws IOException {
        final Thread transactionId = getTransactionId();
        for (final FileContent item : list) {
            if (!locks.acquireWrite(item.id, transactionId)) {
                return item.type + " being changed by another user, please try again\n" + item.data;
            }
            if (Util.shouldFileExist(item.command)) {
                final DataFileReader dataReader = new DataFileReader(item.type, item.id);
                final String version = dataReader.getVersion();
                if (!version.equals(item.currentVersion)) {
                    // String data = dataReader.getData();
                    dataReader.close();
                    return "mismatch between FileContent version (" + item.currentVersion + ") and DataReader version (" + version + ")";
                }
                dataReader.close();
            }
        }
        return null;
    }

    private void releaseLocks(final List<FileContent> list) {
        final Thread transactionId = getTransactionId();
        for (final FileContent item : list) {
            locks.release(item.id, transactionId);
        }
    }

    private Thread getTransactionId() {
        return Thread.currentThread();
    }

    private void status(final ServerConnection connection) throws IOException {
        connection.endCommand();
        final String request = connection.getRequest();
        if (request.equals("contains-data")) {
            connection.response(Util.isPopulated());

        } else {
            connection.error("Unrecognised command " + request);
        }
    }

    private void service(final ServerConnection connection) {
        connection.endCommand();
        final String name = connection.getRequest();
        final File file = Util.serviceFile(name);
        if (file.exists()) {
            final String id = readServiceFile(file);
            connection.response(id);
        } else {
            connection.response("null");
        }
    }

    private String readServiceFile(final File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Util.ENCODING));
            final String[] split = reader.readLine().split(" ");
            return split[1];
        } catch (final IOException e) {
            LOG.error("failed to read service file", e);
            throw new FileServerException("Failed to read service file", e);
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                LOG.error("failed to close file", e);
            }
        }
    }

    private void saveService(final ServerConnection connection) throws IOException {
        connection.endCommand();
        final String name = connection.getRequest();
        final String key = connection.getRequest();
        logger.logServiceEntry(key, name);
        saveService(key, name);
        connection.ok();
    }

    void saveService(final String key, final String name) throws FileNotFoundException, IOException, UnsupportedEncodingException {
        FileOutputStream fileOut = null;
        final File file = Util.serviceFile(name);
        try {
            fileOut = new FileOutputStream(file);
            fileOut.write(name.getBytes(Util.ENCODING));
            fileOut.write(' ');
            fileOut.write(key.getBytes(Util.ENCODING));
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (final IOException e) {
                    throw new NoSqlStoreException(e);
                }
            }
        }
    }

    private void nextSerialBatch(final ServerConnection connection) throws IOException {
        // TODO lock file first

        connection.endCommand();
        final String name = connection.getRequest();
        final int batchSize = connection.getRequestAsInt();

        long nextId;
        final File file = Util.serialNumberFile(name);
        if (!file.exists()) {
            nextId = 1;
            LOG.info("Initial ID batch created at " + nextId);
        } else {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Util.ENCODING));
            nextId = Long.valueOf(reader.readLine()).longValue();
            reader.close();
            LOG.info("New ID batch allocated, from " + nextId);
        }

        final long newBatchAt = nextId + batchSize;
        logger.logNextSerialBatch(name, newBatchAt);

        saveNextBatch(file, newBatchAt);

        // TODO remove lock

        connection.response(nextId);
    }

    private void saveNextBatch(final File file, final long newBatchAt) throws FileNotFoundException, IOException {
        final FileOutputStream fileOutput = new FileOutputStream(file);
        fileOutput.write(Long.toString(newBatchAt).getBytes(Util.ENCODING));
        fileOutput.close();
    }

    public void saveNextBatch(final String name, final long nextBatch) throws IOException {
        saveNextBatch(Util.serialNumberFile(name), nextBatch);
    }

    private void hasInstances(final ServerConnection connection) throws IOException {
        connection.endCommand();
        final String type = connection.getRequest();
        final File[] listFiles = listFiles(type);
        final boolean hasInstances = listFiles != null && listFiles.length > 0;
        connection.response(hasInstances);
    }

}
