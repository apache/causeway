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
package org.apache.isis.valuetypes.sse.ui.wkt.webmodule;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.value.Markup;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.valuetypes.sse.applib.service.SseChannel;
import org.apache.isis.valuetypes.sse.applib.service.SseService;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Server-sent events.
 *
 * @see <a href="https://www.w3schools.com/html/html5_serversentevents.asp">www.w3schools.com</a>
 *
 * @since 2.0
 *
 */
//@WebServlet(value="/sse", asyncSupported=true)
@Log4j2
public class ServerSentEventsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Autowired private SseService sseService;

    @Override
    public void init() throws ServletException {
        super.init();
        Objects.requireNonNull(sseService, "sseService");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        val eventStreamType = parseEventStreamType(request);
        val eventStream = eventStreamType.flatMap(sseService::lookupByType)
                .orElse(null);

        response.setStatus(200);

        if(eventStream==null) {
            response.setContentType(null);
            flushBuffer(response);
            return;
        }

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        //response.setHeader("Access-Control-Allow-Origin", "*"); // not secure, can be used for debugging
        response.setHeader("Cache-Control", "no-cache,no-store");

        if(!flushBuffer(response)) {
            return;
        }

        asyncContext(request)
        .ifPresent(asyncContext->{

            // javac explicitly requires curly-braces here (to tell it we want a Runnable)
            ForkJoinPool.commonPool().submit(()->{
                fork(asyncContext, eventStream);
            });

        });

    }

    // -- HELPER

    private Optional<AsyncContext> asyncContext(final HttpServletRequest request) {
        try {
            return Optional.of(request.startAsync());
        } catch (IllegalStateException e) {
            log.warn("failed to put request into asynchronous mode", e);
            return Optional.empty();
        }
    }

    private boolean flushBuffer(final HttpServletResponse response) {
        try {
            response.flushBuffer();
            return true;
        } catch (IOException e) {
            log.warn("failed to flush response buffer", e);
        }
        return false;
    }

    private void fork(final AsyncContext asyncContext, final SseChannel eventStream) {

        val response = asyncContext.getResponse();
        val marshaller = new Markup.JaxbToStringAdapter();

        eventStream.listenWhile(source->{

            if(ForkJoinPool.commonPool().isShutdown()) {
                return false; // stop listening
            }

            try {

                val writer = response.getWriter(); // don't close the writer, its likely to be reused
                if(writer==null) {
                    return false; // stop listening
                }

                val payload = marshaller.marshal(Markup.valueOf(source.getPayload()));

                writer
                .append("data: ")
                .append(payload)
                .append("\n\n")
                .flush();

                return true; // continue listening

            } catch (Exception e) {
                log.warn("failed to run the fork task", e);
                return false; // stop listening
            }

        });

        // now we wait until the eventStream closes
        try {
            eventStream.awaitClose();
        } catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

        // Completes the asynchronous operation that was started on the request
        // that was used to initialize this AsyncContext.
        asyncContext.complete();

    }

    private Optional<Class<?>> parseEventStreamType(final HttpServletRequest request) {
        val eventStreamId = request.getParameter("eventStream");
        if(_Strings.isNullOrEmpty(eventStreamId)) {
            return Optional.empty();
        }
        try {
            return Optional.of(_Context.loadClass(eventStreamId));
        } catch (Throwable e) {

            log.warn("failed to resolve class by event stream id {}", eventStreamId, e);

            return Optional.empty();
        }
    }

}
