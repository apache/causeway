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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;
import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer.Scope;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

/**
 * Classifies the Java-agent-created HTTP entry span before request processing.
 *
 * @since 2.2
 */
public final class CausewayForegroundTraceFilter implements Filter {

    private final Consumer<ExecutionMode> classifier;
    private final Supplier<Scope> semanticTraceScopeFactory;

    public CausewayForegroundTraceFilter() {
        this(
                CausewayTraceClassifier::classifyCurrentSpan,
                CausewaySemanticTraceNamer::openCurrentSpan);
    }

    CausewayForegroundTraceFilter(final Consumer<ExecutionMode> classifier) {
        this(classifier, CausewaySemanticTraceNamer::openCurrentSpan);
    }

    CausewayForegroundTraceFilter(
            final Consumer<ExecutionMode> classifier,
            final Supplier<Scope> semanticTraceScopeFactory) {
        this.classifier = Objects.requireNonNull(classifier, "classifier");
        this.semanticTraceScopeFactory = Objects.requireNonNull(
                semanticTraceScopeFactory, "semanticTraceScopeFactory");
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        classifier.accept(ExecutionMode.FOREGROUND);
        try (Scope ignored = semanticTraceScopeFactory.get()) {
            chain.doFilter(request, response);
        }
    }
}
