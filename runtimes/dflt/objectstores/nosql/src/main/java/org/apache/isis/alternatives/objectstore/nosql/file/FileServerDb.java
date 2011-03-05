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


package org.apache.isis.alternatives.objectstore.nosql.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.alternatives.objectstore.nosql.NoSqlCommandContext;
import org.apache.isis.alternatives.objectstore.nosql.NoSqlDataDatabase;
import org.apache.isis.alternatives.objectstore.nosql.StateReader;
import org.apache.isis.runtimes.dflt.runtime.persistence.ConcurrencyException;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;


public class FileServerDb implements NoSqlDataDatabase {

    private static final Logger LOG = Logger.getLogger(FileServerDb.class);

    private final String host;
    private final int port;

    private int timeout;

    public FileServerDb(String host, int port, int timeout) {
        this.host = host;
        this.port = port == 0 ? 9012 : port;
        this.timeout = timeout;
    }

    // TODO pool connection and reuse
    private ClientConnection getConnection() {
        return new ClientConnection(host, port, timeout);
    }

    // TODO pool connection and reuse
    private void returnConnection(ClientConnection connection) {
        connection.logComplete();
        connection.close();
    }

    // TODO pool connection and reuse - probably need to replace the connection
    private void abortConnection(ClientConnection connection) {
        connection.logFailure();
        connection.close();
    }

    public StateReader getInstance(String key, String specificationName) {
        ClientConnection connection = getConnection();
        String data;
        try {
            String request = specificationName + " " + key;
            connection.request('R', request);
            connection.validateRequest();
            data = connection.getResponseData();
        } catch (RuntimeException e) {
            LOG.error("aborting getInstance", e);
            abortConnection(connection);
            throw e;
        }
        JsonStateReader reader = new JsonStateReader(data);
        returnConnection(connection);
        return reader;
    }

    public Iterator<StateReader> instancesOf(String specificationName) {
        ClientConnection connection = getConnection();
        List<StateReader> instances;
        try {
            instances = new ArrayList<StateReader>();
            connection.request('L', specificationName + " 0");
            connection.validateRequest();
            String data;
            while ((data = connection.getResponseData()).length() > 0) {
                JsonStateReader reader = new JsonStateReader(data);
                instances.add(reader);
            }
        } catch (RuntimeException e) {
            LOG.error("aborting instancesOf", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
        return instances.iterator();

    }

    public void write(List<PersistenceCommand> commands) {
        ClientConnection connection = getConnection();
        try {
            connection.request('W', "");
            NoSqlCommandContext context = new FileClientCommandContext(connection);
            for (PersistenceCommand command : commands) {
                command.execute(context);
            }
            connection.validateRequest();
            
        } catch (ConcurrencyException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.error("aborting write", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
    }

    public void close() {}

    public void open() {}

    public boolean containsData() {
        ClientConnection connection = getConnection();
        boolean flag;
        try {
            connection.request('X', "contains-data");
            connection.validateRequest();
            flag = connection.getResponseAsBoolean();
        } catch (RuntimeException e) {
            LOG.error("aborting containsData", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
        return flag;
    }

    public long nextSerialNumberBatch(int batchSize) {
        ClientConnection connection = getConnection();
        long serialNumber;
        try {
            connection.request('N', Integer.toString(batchSize));
            connection.validateRequest();
            serialNumber = connection.getResponseAsLong();
        } catch (RuntimeException e) {
            LOG.error("aborting nextSerialNumberBatch", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
        return serialNumber;
    }

    public void addService(String name, String key) {
        ClientConnection connection = getConnection();
        try {
            connection.request('T', name + " " + key);
            connection.validateRequest();
        } catch (RuntimeException e) {
            LOG.error("aborting addService", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
    }

    public String getService(String name) {
        ClientConnection connection = getConnection();
        String response;
        try {
            connection.request('S', name);
            connection.validateRequest();
            response = connection.getResponse();
        } catch (RuntimeException e) {
            LOG.error("aborting getServices", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
        return response.equals("null") ? null : response;
    }

    public boolean hasInstances(String specificationName) {
        ClientConnection connection = getConnection();
        boolean hasInstances;
        try {
            connection.request('I', specificationName);
            connection.validateRequest();
            hasInstances = connection.getResponseAsBoolean();
        } catch (RuntimeException e) {
            LOG.error("aborting hasInstances", e);
            abortConnection(connection);
            throw e;
        }
        returnConnection(connection);
        return hasInstances;
    }
}

