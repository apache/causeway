package org.apache.causeway.viewer.graphql.model.domain;

import graphql.GraphQLContext;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;

import lombok.Getter;

import static graphql.schema.GraphQLFieldDefinition.*;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;

public class GqlvScenarioName extends GqlvAbstract {

    private final Holder holder;

    public GqlvScenarioName(
            final GqlvScenarioName.Holder holder,
            final Context context) {

        super(context);

        this.holder = holder;

        setField(newFieldDefinition()
                    .name("Name")
                    .type(Scalars.GraphQLString)
                    .build()
        );
    }

    public void addDataFetchers() {
        context.codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(getField()),
                (DataFetcher<Object>) environment ->  name(environment));
    }

    private String name(DataFetchingEnvironment environment) {
        // TODO: use graphQlContext instead.
        GraphQLContext graphQlContext = environment.getGraphQlContext();
        return context.serviceRegistry.lookupService(Scenario.class).map(Scenario::getName).orElseThrow();
    }

    public interface Holder
            extends GqlvHolder {
    }

}
