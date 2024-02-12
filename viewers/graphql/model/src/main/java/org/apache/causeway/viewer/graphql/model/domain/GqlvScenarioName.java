package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import lombok.Getter;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;

public class GqlvScenarioName  {

    private final Holder holder;
    private final Context context;
    @Getter private final GraphQLFieldDefinition field;

    public GqlvScenarioName(
            final GqlvScenarioName.Holder holder,
            final Context context) {

        this.holder = holder;
        this.context = context;

        this.field = GraphQLFieldDefinition.newFieldDefinition().name("Name").type(Scalars.GraphQLString).build();
    }

    public void addDataFetchers() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                (DataFetcher<Object>) environment ->  name(environment));
    }

    private String name(DataFetchingEnvironment environment) {
        return context.serviceRegistry.lookupService(Scenario.class).map(Scenario::getName).orElseThrow();
    }

    public interface Holder
            extends GqlvHolder {
    }

}
