package org.apache.causeway.viewer.graphql.model.domain;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;

import lombok.val;

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
                this::scenarioName);
    }

    private String scenarioName(DataFetchingEnvironment environment) {
        val graphQlContext = environment.getGraphQlContext();
        return graphQlContext.get(GqlvScenario.KEY_SCENARIO_NAME);
    }

}
