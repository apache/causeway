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

package org.apache.isis.runtimes.dflt.remoting.transport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.runtimes.dflt.monitoring.servermonitor.Monitor;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.OpenSessionRequest;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.Request;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResponseEnvelope;
import org.apache.isis.runtimes.dflt.remoting.server.ServerConnection;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.log4j.Logger;

/**
 * Standard processing for processing an inbound {@link Request} and generating an outbound {@link ResponseEnvelope} (or
 * some sort of {@link Exception}).
 * 
 * <p>
 * Used by the (socket transport) {@link Worker} and originally inlined; now factored out so can be reused by other
 * transports, notably http.
 */
public class ServerConnectionHandler {

    private static final Logger LOG = Logger.getLogger(ServerConnectionHandler.class);
    private static final Logger ACCESS_LOG = Logger.getLogger("access_log");

    private final ServerConnection connection;

    private String debugRequest;
    private String debugAuthSession;
    private String debugResponse;
    private String debugContextId;

    private DebuggableWithTitle[] debugSessionInfo;

    private long responseTime;

    public ServerConnectionHandler(final ServerConnection connection) {
        this.connection = connection;
    }

    // /////////////////////////////////////////////////////
    // handleRequest
    // /////////////////////////////////////////////////////

    public void handleRequest() throws IOException {
        final long start = System.currentTimeMillis();
        final Request request = connection.readRequest();
        AuthenticationSession authenticationSession = null;
        try {
            authenticationSession = openSessionIfNotAuthenticateRequest(request);

            monitorRequest(authenticationSession, request);
            executeRequest(request);

            sendResponse(request);

        } catch (final Exception e) {
            sendExceptionResponse(e);

        } finally {
            closeSessionIfNotAuthenticateRequest(authenticationSession);
            calcResponseTime(start);
        }
    }

    private AuthenticationSession openSessionIfNotAuthenticateRequest(final Request request) {
        AuthenticationSession authenticationSession;
        if (LOG.isDebugEnabled()) {
            debugRequest = request.getId() + " - " + request.toString();
        }
        authenticationSession = request.getSession();

        if (authenticationSession == null) {
            if (LOG.isDebugEnabled()) {
                debugAuthSession = "(none)";
                debugContextId = "(none)";
            }

            if (!(request instanceof OpenSessionRequest)) {
                throw new IsisException(
                    "AuthenticationSession required for all requests (except the initial Authenticate request)");
            }
        } else {
            IsisContext.openSession(authenticationSession);

            if (LOG.isDebugEnabled()) {
                debugAuthSession = authenticationSession.toString();
                debugContextId = IsisContext.getSessionId();
                debugSessionInfo = IsisContext.debugSession();
            }
        }
        return authenticationSession;
    }

    private void monitorRequest(final AuthenticationSession authenticationSession, final Request request) {
        final String userName =
            authenticationSession != null ? authenticationSession.getUserName() : "**AUTHENTICATING**";
        final String message = "{" + userName + "|" + this + "}  " + request.toString();
        ACCESS_LOG.info(message);
        Monitor.addEvent("REQUEST", message, debugSessionInfo);
    }

    private void executeRequest(final Request request) {
        request.execute(connection.getServerFacade());
    }

    private void sendResponse(final Request request) throws IOException {
        final ResponseEnvelope response = new ResponseEnvelope(request);

        if (LOG.isDebugEnabled()) {
            debugResponse = response.toString();
            LOG.debug("sending " + debugResponse);
        }
        connection.sendResponse(response);
    }

    private void sendExceptionResponse(final Exception e) throws IOException {
        LOG.error("error during remote request", e);
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        if (LOG.isDebugEnabled()) {
            debugResponse = sw.toString();
        }

        connection.sendResponse(e);
    }

    private void calcResponseTime(final long start) {
        responseTime = System.currentTimeMillis() - start;
    }

    private void closeSessionIfNotAuthenticateRequest(final AuthenticationSession authenticationSession) {
        if (authenticationSession == null) {
            return;
        }
        IsisContext.closeSession();
    }

    // /////////////////////////////////////////////////////
    // Debug
    // /////////////////////////////////////////////////////

    public void debug(final DebugBuilder debug) {
        debug.appendln("context Id", debugContextId);
        debug.appendln("authSession", debugAuthSession);
        debug.appendln("request", debugRequest);
        debug.appendln("response", debugResponse);
        debug.appendln("duration", responseTime / 1000.0f + " secs.");

        // TODO: the code below was commented out (by Rob, presumably?); I've
        // reinstated it but disabled it so no change in behaviour
        if (false) {
            debugSessionInfo(debug);
        }
    }

    private void debugSessionInfo(final DebugBuilder debug) {
        try {
            if (debugSessionInfo != null) {
                for (final DebuggableWithTitle info : debugSessionInfo) {
                    debug.appendTitle(info.debugTitle());
                    info.debugData(debug);
                }
            }
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }
    }

}
