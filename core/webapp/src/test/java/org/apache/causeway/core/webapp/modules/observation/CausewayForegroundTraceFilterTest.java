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
package org.apache.causeway.core.webapp.modules.observation;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer.Scope;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CausewayForegroundTraceFilterTest {

    @Test
    void classifiesBeforeDelegating() throws Exception {
        @SuppressWarnings("unchecked")
        final Consumer<ExecutionMode> classifier = mock(Consumer.class);
        final FilterChain chain = mock(FilterChain.class);
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);
        final Scope scope = mock(Scope.class);
        @SuppressWarnings("unchecked")
        final Supplier<Scope> scopeFactory = mock(Supplier.class);
        when(scopeFactory.get()).thenReturn(scope);
        final CausewayForegroundTraceFilter filter =
                new CausewayForegroundTraceFilter(classifier, scopeFactory);

        filter.doFilter(request, response, chain);

        final var inOrder = inOrder(classifier, scopeFactory, chain, scope);
        inOrder.verify(classifier).accept(ExecutionMode.FOREGROUND);
        inOrder.verify(scopeFactory).get();
        inOrder.verify(chain).doFilter(request, response);
        inOrder.verify(scope).close();
    }

    @Test
    void preservesDownstreamFailure() throws Exception {
        @SuppressWarnings("unchecked")
        final Consumer<ExecutionMode> classifier = mock(Consumer.class);
        final FilterChain chain = mock(FilterChain.class);
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);
        final IOException failure = new IOException("request failed");
        doThrow(failure).when(chain).doFilter(request, response);
        final Scope scope = mock(Scope.class);
        @SuppressWarnings("unchecked")
        final Supplier<Scope> scopeFactory = mock(Supplier.class);
        when(scopeFactory.get()).thenReturn(scope);
        final CausewayForegroundTraceFilter filter =
                new CausewayForegroundTraceFilter(classifier, scopeFactory);

        final IOException thrown = assertThrows(
                IOException.class,
                () -> filter.doFilter(request, response, chain));

        assertSame(failure, thrown);
        verify(classifier).accept(ExecutionMode.FOREGROUND);
        verify(scope).close();
    }

    @Test
    void nestedDispatchScopesCloseInReverseOrder() throws Exception {
        @SuppressWarnings("unchecked")
        final Consumer<ExecutionMode> classifier = mock(Consumer.class);
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);
        final FilterChain outerChain = mock(FilterChain.class);
        final FilterChain innerChain = mock(FilterChain.class);
        final Scope outerScope = mock(Scope.class);
        final Scope innerScope = mock(Scope.class);
        @SuppressWarnings("unchecked")
        final Supplier<Scope> scopeFactory = mock(Supplier.class);
        when(scopeFactory.get()).thenReturn(outerScope, innerScope);
        final CausewayForegroundTraceFilter filter =
                new CausewayForegroundTraceFilter(classifier, scopeFactory);
        doAnswer(invocation -> {
            filter.doFilter(request, response, innerChain);
            return null;
        }).when(outerChain).doFilter(request, response);

        filter.doFilter(request, response, outerChain);

        final var inOrder = inOrder(innerChain, innerScope, outerScope);
        inOrder.verify(innerChain).doFilter(request, response);
        inOrder.verify(innerScope).close();
        inOrder.verify(outerScope).close();
    }

    @Test
    void noAgentCurrentSpanIsSafe() throws Exception {
        final FilterChain chain = mock(FilterChain.class);
        final ServletRequest request = mock(ServletRequest.class);
        final ServletResponse response = mock(ServletResponse.class);

        new CausewayForegroundTraceFilter().doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void webModuleRegistersFilterForAllPaths() throws Exception {
        final ServiceInjector serviceInjector = mock(ServiceInjector.class);
        final ServletContext servletContext = mock(ServletContext.class);
        final FilterRegistration.Dynamic registration = mock(FilterRegistration.Dynamic.class);
        final CausewayForegroundTraceFilter filter = new CausewayForegroundTraceFilter();
        when(servletContext.createFilter(CausewayForegroundTraceFilter.class)).thenReturn(filter);
        when(servletContext.addFilter("CausewayForegroundTraceFilter", filter)).thenReturn(registration);

        new WebModuleTraceClassification(serviceInjector).init(servletContext);

        verify(serviceInjector).injectServicesInto(filter);
        verify(registration).addMappingForUrlPatterns(null, false, "/*");
    }
}
