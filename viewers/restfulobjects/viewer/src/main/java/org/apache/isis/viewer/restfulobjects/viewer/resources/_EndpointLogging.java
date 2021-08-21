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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;

import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

import lombok.experimental.UtilityClass;

@UtilityClass
class _EndpointLogging {


    /**
     * Returns given {@code stringResponse} untampered.
     */
    String stringResponse(
            final Logger log,
            final String format,
            final String stringResponse) {
        if(log.isDebugEnabled()) {
            logRequest(log, format);
            logResponse(log, stringResponse);
        }
        return stringResponse;
    }

    /**
     * Returns given {@code Response} untampered.
     */
    Response response(
            final Logger log,
            final String format,
            final Response response) {
        if(log.isDebugEnabled()) {
            logRequest(log, format);
            logResponse(log, response);
        }
        return response;
    }

    /**
     * Returns given {@code Response} untampered.
     */
    Response response(
            final Logger log,
            final String format,
            final String arg0,
            final Response response) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0);
            logResponse(log, response);
        }
        return response;
    }

    /**
     * Returns given {@code Response} untampered.
     */
    Response response(
            final Logger log,
            final String format,
            final String arg0,
            final String arg1,
            final Response response) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0, arg1);
            logResponse(log, response);
        }
        return response;
    }

    /**
     * Returns given {@code Response} untampered.
     */
    Response response(
            final Logger log,
            final String format,
            final String arg0,
            final String arg1,
            final String arg2,
            final Response response) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0, arg1, arg2);
            logResponse(log, response);
        }
        return response;
    }

    /**
     * Returns given {@code RestfulObjectsApplicationException} untampered.
     */
    RestfulObjectsApplicationException error(
            final Logger log,
            final String format,
            final RestfulObjectsApplicationException roException) {
        if(log.isDebugEnabled()) {
            logRequest(log, format);
            logError(log, roException);
        }
        return roException;
    }

    /**
     * Returns given {@code RestfulObjectsApplicationException} untampered.
     */
    RestfulObjectsApplicationException error(
            final Logger log,
            final String format,
            final String arg0,
            final RestfulObjectsApplicationException roException) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0);
            logError(log, roException);
        }
        return roException;
    }

    /**
     * Returns given {@code RestfulObjectsApplicationException} untampered.
     */
    RestfulObjectsApplicationException error(
            final Logger log,
            final String format,
            final String arg0,
            final String arg1,
            final RestfulObjectsApplicationException roException) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0, arg1);
            logError(log, roException);
        }
        return roException;
    }

    /**
     * Returns given {@code RestfulObjectsApplicationException} untampered.
     */
    RestfulObjectsApplicationException error(
            final Logger log,
            final String format,
            final String arg0,
            final String arg1,
            final String arg2,
            final RestfulObjectsApplicationException roException) {
        if(log.isDebugEnabled()) {
            logRequest(log, format, arg0, arg1, arg2);
            logError(log, roException);
        }
        return roException;
    }

    // -- HELPER

    private void logRequest(final Logger log, final String format, final Object... args) {
        log.debug(">>> REQUEST");
        log.debug(format, args);
    }

    private void logResponse(final Logger log, final Response response) {
        log.debug("<<< RESPONSE");
        log.debug(response.getEntity());
        log.debug("--- END RESPONSE");
    }

    private void logResponse(final Logger log, final String stringResponse) {
        log.debug("<<< RESPONSE");
        log.debug(stringResponse);
        log.debug("--- END RESPONSE");
    }

    private void logError(final Logger log, final RestfulObjectsApplicationException roException) {
        log.debug("<<< ERROR");
        log.debug(roException.getBody());
        log.debug("--- END ERROR");
    }

}
