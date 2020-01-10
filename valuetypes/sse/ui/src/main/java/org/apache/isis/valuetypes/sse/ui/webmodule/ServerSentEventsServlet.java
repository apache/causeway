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
package org.apache.isis.valuetypes.sse.ui.webmodule;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.util.JaxbAdapters;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.valuetypes.sse.applib.service.SseChannel;
import org.apache.isis.valuetypes.sse.applib.service.SseService;
import org.apache.isis.valuetypes.sse.applib.value.ListeningMarkup;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

/**
 * Server-sent events.
 *  
 * @see <a href="https://www.w3schools.com/html/html5_serversentevents.asp">Server Sent Events (@ w3schools)</a>
 * 
 * @since 2.0
 *
 */
//@WebServlet(value="/sse", asyncSupported=true)
public class ServerSentEventsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ExecutorService threadPool;

    @Autowired private SseService sseService;

    @Override
    public void init() throws ServletException {
        super.init();
        threadPool = ForkJoinPool.commonPool();
        
        requires(sseService, "sseService");
    }

    @Override
    public void destroy() {
        threadPool.shutdown();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        val eventStreamType = parseEventStreamType(request);
        val eventStream = eventStreamType.flatMap(sseService::lookupByType)
                .orElse(null);

        if(eventStream==null) {
            response.setStatus(200);
            response.setContentType(null);
            response.flushBuffer();
            return;
        }

        response.setStatus(200);
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        //response.setHeader("Access-Control-Allow-Origin", "*"); // not secure, can be used for debugging
        response.setHeader("Cache-Control", "no-cache,no-store");
        response.flushBuffer();

        final AsyncContext asyncContext = request.startAsync();

        //XXX javac explicitly requires curly-braces here (to tell it we want a Runnable)
        threadPool.submit(()->{fork(asyncContext, eventStream);});

    }

    // -- HELPER

    private void fork(final AsyncContext asyncContext, final SseChannel eventStream) {

        val response = asyncContext.getResponse();
        val marshaller = new JaxbAdapters.MarkupAdapter();

        eventStream.listenWhile(source->{

            if(threadPool.isShutdown()) {
                return false; // stop listening
            }

            try {

                val writer = response.getWriter(); // don't close the writer, its likely to be reused
                if(writer==null) {
                    return false; // stop listening
                }

                val payload = marshaller.marshal(ListeningMarkup.valueOfHtml(source.getPayload()));

                writer
                .append("data: ")
                .append(payload)
                .append("\n\n")
                .flush();

                return true; // continue listening                

            } catch (Exception e) {
                e.printStackTrace();
                return false; // stop listening
            }

        });

        // now we wait until the eventStream closes
        try {
            eventStream.awaitClose();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Completes the asynchronous operation that was started on the request
        // that was used to initialize this AsyncContext. 
        asyncContext.complete();

    }

    private Optional<Class<?>> parseEventStreamType(HttpServletRequest request) {
        val eventStreamId = request.getParameter("eventStream");
        if(_Strings.isNullOrEmpty(eventStreamId)) {
            return Optional.empty();
        }
        try {
            return Optional.of(_Context.loadClass(eventStreamId));
        } catch (Throwable e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
