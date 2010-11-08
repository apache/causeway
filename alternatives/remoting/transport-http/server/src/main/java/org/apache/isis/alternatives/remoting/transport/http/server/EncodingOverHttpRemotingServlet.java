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

package org.apache.isis.alternatives.remoting.transport.http.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.alternatives.remoting.marshalling.encode.EncodingMarshaller;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.webapp.IsisWebAppBootstrapper;
import org.apache.isis.remoting.facade.impl.ServerFacadeImpl;
import org.apache.isis.remoting.protocol.internal.ObjectEncoderDecoderDefault;
import org.apache.isis.remoting.server.ServerConnection;
import org.apache.isis.remoting.server.ServerConnectionDefault;
import org.apache.isis.remoting.transport.ServerConnectionHandler;
import org.apache.isis.remoting.transport.simple.SimpleTransport;
import org.apache.isis.runtime.system.IsisSystem;

/**
 * Analogous to {@link SocketsViewerAbstract}; both ultimately delegate to {@link ServerConnectionHandler}.
 */
public class EncodingOverHttpRemotingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IsisSystem system;
    private IsisConfiguration configuration;

    private ServerFacadeImpl serverFacade;

    @Override
    public void init() throws ServletException {
        super.init();
        system = IsisWebAppBootstrapper.getSystemBoundTo(getServletContext());
        configuration = system.getConfiguration();

        serverFacade = new ServerFacadeImpl(system.getSessionFactory().getAuthenticationManager());
        serverFacade.setEncoder(ObjectEncoderDecoderDefault.create(configuration));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        ServletInputStream inputStream = request.getInputStream();
        ServletOutputStream outputStream = response.getOutputStream();

        ServerConnection serverConnection = createConnection(inputStream, outputStream);

        try {
            new ServerConnectionHandler(serverConnection).handleRequest();
        } catch (IOException ex) {
            // REVIEW: is this enough, or should we try to return a more
            // user-friendly exception or status?
            throw ex;
        } finally {
            // nothing to do
        }
    }

    private ServerConnection createConnection(ServletInputStream inputStream, ServletOutputStream outputStream)
        throws IOException {

        // TODO: should use installers to create these,
        // provides the opportunity to read in installer-specific config files.
        SimpleTransport transport = new SimpleTransport(configuration, inputStream, outputStream);
        EncodingMarshaller marshaller = new EncodingMarshaller(configuration, transport);

        // this is a no-op with the SimpleTransport, but include for consistency
        marshaller.connect();

        return new ServerConnectionDefault(serverFacade, marshaller);
    }
}
