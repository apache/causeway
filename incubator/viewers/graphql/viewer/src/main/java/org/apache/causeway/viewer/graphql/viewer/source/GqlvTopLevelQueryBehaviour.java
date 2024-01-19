package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;

import org.apache.causeway.applib.services.registry.ServiceRegistry;

import static graphql.schema.FieldCoordinates.coordinates;

public class GqlvTopLevelQueryBehaviour {

    private final GqlvTopLevelQueryStructure structure;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final ServiceRegistry serviceRegistry;

    public GqlvTopLevelQueryBehaviour(
            final GqlvTopLevelQueryStructure structure,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final ServiceRegistry serviceRegistry) {
        this.structure = structure;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.serviceRegistry = serviceRegistry;
    }

    public void addFetchers() {
        codeRegistryBuilder
                .dataFetcher(
                        coordinates(structure.getQueryType(), structure.getNumServicesField()),
                        (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count());
    }

}
