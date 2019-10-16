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

package org.apache.isis.core.webapp;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.servlet.TracingFilter;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class TracingZipkinFilter implements Filter {

    Sender sender;
    AsyncReporter<Span> spanReporter;
    Tracing tracing;
    Filter delegate ;
    ExtraFieldPropagation.Factory propagationFactory;
    Tracer tracer;

    @Override
    public void init(final FilterConfig filterConfig) {

        final String tracingEndpoint = initParamFrom(filterConfig, "tracingEndpoint", null);
        if(tracingEndpoint == null) {
            return;
        }
        sender = OkHttpSender.create(tracingEndpoint);
        spanReporter = AsyncReporter.create(sender);

        propagationFactory = ExtraFieldPropagation.newFactoryBuilder(B3Propagation.FACTORY)
                .addPrefixedFields("baggage-", Arrays.asList("country-code", "user-id"))
                .build();

        final String tracingLocalServiceName = initParamFrom(filterConfig, "tracingLocalServiceName", "apache-isis-app");
        tracing = Tracing.newBuilder()
                .localServiceName(tracingLocalServiceName)
                .propagationFactory(propagationFactory)
                .spanReporter(spanReporter)
                .build();

        tracer = tracing.tracer();

//        tracer = BraveTracer.create(tracing);
        delegate = TracingFilter.create(tracing);
    }

    private static String initParamFrom(
            final FilterConfig filterConfig,
            final String paramName,
            final String defaultValue) {
        final String value = filterConfig.getInitParameter(paramName);
        return value != null ? value : defaultValue;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if(delegate != null) {

            request.setAttribute("isis.tracer.zipkin.brave", tracer);

            delegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        try {
            if(tracing != null) {
                tracing.close(); // disables Tracing.current()
            }
            if(spanReporter != null) {
                spanReporter.close(); // stops reporting thread and flushes data
            }
            if(sender != null) {
                sender.close(); // closes any transport resources
            }
        } catch (IOException e) {
            // do something real
        }
    }
}
