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


package org.apache.isis.alternatives.remoting.transport.pipe;

import org.apache.isis.alternatives.remoting.common.exchange.Request;
import org.apache.isis.alternatives.remoting.common.exchange.ResponseEnvelope;
import org.apache.log4j.Logger;


public class PipedConnection {
    private static final Logger LOG = Logger.getLogger(PipedConnection.class);
    private Request request;
    private ResponseEnvelope response;
    private RuntimeException exception;

    public synchronized void setRequest(final Request request) {
        this.request = request;
        notify();
    }

    public synchronized Request getRequest() {
        while (request == null) {
            try {
                wait();
            } catch (final InterruptedException e) {
                LOG.error("wait (getRequest) interrupted", e);
            }
        }

        final Request r = request;
        request = null;
        notify();
        return r;
    }

    public synchronized void setResponse(final ResponseEnvelope response) {
        this.response = response;
        notify();
    }

    public synchronized void setException(final RuntimeException exception) {
        this.exception = exception;
        notify();
    }

    public synchronized ResponseEnvelope getResponse() {
        while (response == null && exception == null) {
            try {
                wait();
            } catch (final InterruptedException e) {
                LOG.error("wait (getResponse) interrupted", e);
            }
        }

        if (exception != null) {
            final RuntimeException toThrow = exception;
            exception = null;
            throw toThrow;
        }

        final ResponseEnvelope r = response;
        response = null;
        notify();
        return r;
    }

}
