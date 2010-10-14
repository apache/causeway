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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.extensions.nosql.NoSqlStoreException;


public class FileServerProcessor {

    private static final Logger LOG = Logger.getLogger(FileServerProcessor.class);

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

    public void process(ServerConnection connection) {
        try {
            if (acceptNewRequests) {
                char command = connection.getCommand();
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
        } catch (Exception e) {
            LOG.error("Request failed", e);
            connection.error("Remote exception thrown:\n" + e.getMessage(), e);

        } finally {
            connection.close();
        }
    }

    private void list(ServerConnection connection) {
        try {
            String type = connection.getRequest();
            int limit = connection.getRequestAsInt();
            if (limit == 0) {
                limit = Integer.MAX_VALUE;
            }

            File[] listFiles = listFiles(type);
            if (listFiles != null) {
                connection.ok();
                for (File file : listFiles) {
                    String fileName = file.getName();
                    String id = fileName.substring(0, fileName.length() - 5);
                    DataReader reader = findInstance(type, id, connection);
                    readInstance(reader, connection);
                    locks.release(id, getTransactionId());
                    connection.endBlock();
                    if (limit-- < 0) {
                        break;
                    }
                }
                connection.endBlock();
            } else {
                connection.response("");
            }

        } catch (IOException e) {
            throw new NoSqlStoreException(Util.READ_ERROR, e);
        }
    }

    private File[] listFiles(String type) {
        File[] listFiles = Util.directory(type).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".data");
            }
        });
        return listFiles;
    }

    private void read(ServerConnection connection) {
        String type = null;
        String id = null;
        try {
            type = connection.getRequest();
            id = connection.getRequest();
            DataReader reader = findInstance(type, id, connection);
            if (reader == null) {
                connection.error(Util.FILE_NOT_FOUND + " for " + type + " & " + id);
            } else {
                connection.ok();
                readInstance(reader, connection);
            }
            locks.release(id, getTransactionId());
        } catch (FileNotFoundException e) {
            // TODO convert these to responses
            throw new NoSqlStoreException(e.getMessage(), e);
        } catch (IOException e) {
            throw new NoSqlStoreException(Util.READ_ERROR + " for " + type + " & " + id, e);
        }
    }


    private DataReader findInstance(String type, String id, ServerConnection connection) throws IOException {
        LOG.debug("reading file " + id);
        locks.acquireRead(id, getTransactionId());
        try {
            return new DataReader(type, id);
        } catch (FileNotFoundException e) {
            // TODO convert these to responses
            throw new NoSqlStoreException(Util.FILE_NOT_FOUND + " for " + type + " & " + id, e);
        }
    }
    
    private void readInstance(DataReader reader, ServerConnection connection) {
        String data = reader.getData();
        reader.close();
        connection.responseData(data);
    }

    private void write(ServerConnection connection) {
        List<FileContent> files = null;
        try {
            files = getWriteRequests(connection);
            String error = acquireLocks(files);
            if (error == null) {
                logger.log(files);
                DataWriter content = new DataWriter(files);
                content.writeData();
                connection.ok();
            } else {
                connection.error(error);
            }

        } catch (IOException e) {
            throw new NoSqlStoreException("Failed to write data", e);
        } finally {
            if (files != null) {
                releaseLocks(files);
            }
        }
    }

    private List<FileContent> getWriteRequests(ServerConnection connection) throws IOException {
        ArrayList<FileContent> files = new ArrayList<FileContent>();
        while(connection.readHeader()) {
            char command = connection.getCommand();
            String type = connection.getRequest();
            String id = connection.getRequest();
            String currentVersion = connection.getRequest();
            String newVersion = connection.getRequest();
            LOG.debug("write for " + type + "@" + id + " v." + newVersion);

            String buf = connection.getData();
            files.add(new FileContent(command, id, currentVersion, newVersion, type, buf));
        }
        return files;
    }

    private String acquireLocks(List<FileContent> list) throws IOException {
        Thread transactionId = getTransactionId();
        for (FileContent item : list) {
            if (!locks.acquireWrite(item.id, transactionId)) {
                return item.type + " being changed by another user, please try again\n" + item.data;
            }
            if (Util.shouldFileExist(item.command)) {
                DataReader dataReader = new DataReader(item.type, item.id);
                String version = dataReader.getVersion();
                if (!version.equals(item.currentVersion)) {
                    String data = dataReader.getData();
                    dataReader.close();
                    return data;
                }
                dataReader.close();
            }
        }
        return null;
    }

    private void releaseLocks(List<FileContent> list) {
        Thread transactionId = getTransactionId();
        for (FileContent item : list) {
            locks.release(item.id, transactionId);
        }
    }

    private Thread getTransactionId() {
        return Thread.currentThread();
    }

    private void status(ServerConnection connection) throws IOException {
        String request = connection.getRequest();
        if (request.equals("contains-data")) {
            connection.response(Util.isPopulated());

        } else {
            connection.error("Unrecognised command " + request);
        }
    }

    private void service(ServerConnection connection) {
        String name = connection.getRequest();
        File file = Util.serviceFile(name);
        if (file.exists()) {
            String id = readServiceFile(file);
            connection.response(id);
        } else {
            connection.response("null");
        }
    }

    private String readServiceFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Util.ENCODING));
            String[] split = reader.readLine().split(" ");
            return split[1];
        } catch (IOException e) {
            LOG.error("failed to read service file", e);
            throw new FileServerException("Failed to read service file", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("failed to close file", e);
            }
        }
    }

    private void saveService(ServerConnection connection) throws IOException {
        String name = connection.getRequest();
        String key = connection.getRequest();

        FileOutputStream fileOut = null;
        File file = Util.serviceFile(name);
        try {
            fileOut = new FileOutputStream(file);
            fileOut.write(name.getBytes("utf-8"));
            fileOut.write(' ');
            fileOut.write(key.getBytes("utf-8"));
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e) {
                    throw new NoSqlStoreException(e);
                }
            }
        }
        connection.ok();
    }

    private void nextSerialBatch(ServerConnection connection) throws IOException {
        // TODO lock file first

        int batchSize = connection.getRequestAsInt();

        long nextId;
        File file = Util.serialNumberFile();
        if (!file.exists()) {
            nextId = 1;
            LOG.info("Initial ID batch created at " + nextId);
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Util.ENCODING));
            nextId = Long.valueOf(reader.readLine()).longValue();
            reader.close();
            LOG.info("New ID batch allocated, from " + nextId);
        }

        FileOutputStream fileOutput = new FileOutputStream(file);
        long newBatchAt = nextId + batchSize;
        fileOutput.write(Long.toString(newBatchAt).getBytes("utf-8"));
        fileOutput.close();

        // TODO remove lock

        connection.response(nextId);
    }

    private void hasInstances(ServerConnection connection) throws IOException {
        String type = connection.getRequest();
        boolean hasInstances = listFiles(type).length > 0;
        connection.response(hasInstances);
    }

}

