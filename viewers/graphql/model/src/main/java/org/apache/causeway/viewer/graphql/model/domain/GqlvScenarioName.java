package org.apache.causeway.viewer.graphql.model.domain;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.viewer.graphql.model.context.Context;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import lombok.val;

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

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        val graphQlContext = environment.getGraphQlContext();
        return graphQlContext.get(GqlvScenario.KEY_SCENARIO_NAME);
    }

}
