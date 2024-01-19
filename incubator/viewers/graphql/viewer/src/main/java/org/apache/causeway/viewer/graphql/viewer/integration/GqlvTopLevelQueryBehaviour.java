package org.apache.causeway.viewer.graphql.viewer.integration;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.viewer.graphql.viewer.source.GqlvTopLevelQueryStructure;

import static graphql.schema.FieldCoordinates.coordinates;

public class GqlvTopLevelQueryBehaviour {

    private final GqlvTopLevelQueryStructure topLevelQueryStructure;
    private final ServiceRegistry serviceRegistry;

    public GqlvTopLevelQueryBehaviour(
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final ServiceRegistry serviceRegistry) {
        this.topLevelQueryStructure = topLevelQueryStructure;
        this.serviceRegistry = serviceRegistry;
    }

    public void addFetchersTo(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        codeRegistryBuilder
                .dataFetcher(
                        coordinates(topLevelQueryStructure.getQueryType(), topLevelQueryStructure.getNumServicesField()),
                        (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count());

    }
}
