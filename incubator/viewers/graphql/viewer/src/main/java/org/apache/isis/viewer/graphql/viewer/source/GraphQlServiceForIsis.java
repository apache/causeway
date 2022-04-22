package org.apache.isis.viewer.graphql.viewer.source;

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
 * Defers calling of {@link GraphQlSourceForIsis#schema()} until after the metamodel is fully introspected.
 */
@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlServiceForIsis implements ExecutionGraphQlService {

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
