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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

/** Classifies the HTTP entry, not the security or viewer observations opened downstream. */
public final class CausewayForegroundTraceFilter extends OncePerRequestFilter {
    private final CausewayTraceClassifier classifier;

    private final CausewaySemanticTraceNamer semanticTraceNamer;

    public CausewayForegroundTraceFilter(CausewayTraceClassifier classifier, CausewaySemanticTraceNamer semanticTraceNamer) {
        this.classifier = classifier;
        this.semanticTraceNamer = semanticTraceNamer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        classifier.classifyCurrentSpan(ExecutionMode.FOREGROUND);
        // Capture the HTTP entry before downstream scopes open. Nominations
        // select its final display without owning the span's lifetime.
        try (var naming = semanticTraceNamer.openCurrentSpan()) {
            chain.doFilter(request, response);
        }
    }
    // OncePerRequestFilter skips async/error redispatch: the entry was already
    // classified, and the current span during redispatch might be a child.
}
