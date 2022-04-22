package org.apache.isis.viewer.graphql.viewer.source;

import javax.inject.Inject;

import org.springframework.graphql.GraphQlService;
import org.springframework.graphql.RequestInput;
import org.springframework.graphql.RequestOutput;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.graphql.execution.ExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * Defers calling of {@link GraphQlSourceForIsis#schema()} until after the metamodel is fully introspected.
 */
@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlServiceForIsis implements GraphQlService {

    private final BatchLoaderRegistry batchLoaderRegistry;
    private final GraphQlSource graphQlSource;

    ExecutionGraphQlService delegate;

    @Override
    public Mono<RequestOutput> execute(RequestInput input) {
        if(delegate == null) {
            delegate = new ExecutionGraphQlService(graphQlSource);
            delegate.addDataLoaderRegistrar(batchLoaderRegistry);
        }
        return delegate.execute(input);
    }
}
