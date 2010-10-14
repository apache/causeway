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


package org.apache.isis.remoting.server;

import java.io.IOException;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.remoting.exchange.Request;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.ServerMarshaller;

public class ServerConnectionDefault implements ServerConnection {

    private final ServerFacade server;
    private ServerMarshaller serverMarshaller;

    public ServerConnectionDefault(final ServerFacade server, final ServerMarshaller serverMarshaller) {
        this.server = server;
        this.serverMarshaller = serverMarshaller; 
    }

    public ServerFacade getServerFacade() {
        return server;
    }

    protected ServerMarshaller getServerMarshaller() {
        return serverMarshaller;
    }

    public Request readRequest() throws IOException {
        return serverMarshaller.readRequest();
    }

    public void sendResponse(Object response) throws IOException {
        serverMarshaller.sendResponse(response);
    }

    public void sendError(IsisException exception) throws IOException {
        serverMarshaller.sendError(exception);
    }


}
