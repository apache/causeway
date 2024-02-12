package org.apache.causeway.viewer.graphql.model.domain;

import graphql.GraphQLContext;
import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import static graphql.schema.GraphQLFieldDefinition.*;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;

public class GqlvScenarioName extends GqlvAbstract {

    public GqlvScenarioName(
            final Context context) {

        super(context);

        setField(newFieldDefinition()
                    .name("Name")
                    .type(Scalars.GraphQLString)
                    .build()
        );
    }

    public void addDataFetchers(Parent parent) {
        context.codeRegistryBuilder.dataFetcher(
                parent.coordinatesFor(getField()),
                (DataFetcher<Object>) environment ->  name(environment));
    }

    private String name(DataFetchingEnvironment environment) {
        // TODO: use graphQlContext instead.
        GraphQLContext graphQlContext = environment.getGraphQlContext();
        return context.serviceRegistry.lookupService(Scenario.class).map(Scenario::getName).orElseThrow();
    }

}
