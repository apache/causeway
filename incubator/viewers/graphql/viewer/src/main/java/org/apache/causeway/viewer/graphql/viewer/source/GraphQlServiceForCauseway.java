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
package org.apache.causeway.viewer.graphql.viewer.source;

import javax.inject.Inject;

import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.graphql.ExecutionGraphQlResponse;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Defers calling of {@link GraphQlSourceForCauseway#schema()} until after the metamodel is fully introspected.
 */
@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlServiceForCauseway implements ExecutionGraphQlService {

    private final BatchLoaderRegistry batchLoaderRegistry;
    private final GraphQlSource graphQlSource;

    DefaultExecutionGraphQlService delegate;

    @Override
    public Mono<ExecutionGraphQlResponse> execute(final ExecutionGraphQlRequest request) {
        if(delegate == null) {
            delegate = new DefaultExecutionGraphQlService(graphQlSource);
            delegate.addDataLoaderRegistrar(batchLoaderRegistry);
        }
        return delegate.execute(request);
    }
}
