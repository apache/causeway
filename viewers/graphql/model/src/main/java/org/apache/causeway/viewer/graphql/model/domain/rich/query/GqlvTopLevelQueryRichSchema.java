package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvScenario;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvTopLevelQueryAbstractSchema;
import org.apache.causeway.viewer.graphql.model.domain.rich.SchemaStrategyRich;

public class GqlvTopLevelQueryRichSchema
        extends GqlvTopLevelQueryAbstractSchema {

    private static final SchemaStrategy STRATEGY_RICH = new SchemaStrategyRich();

    private final GqlvScenario scenario;

    public GqlvTopLevelQueryRichSchema(final Context context) {
        super(STRATEGY_RICH, context);

        var graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        if (graphqlConfiguration.isIncludeTestingFieldInRich()) {
            addChildFieldFor(scenario = new GqlvScenario(STRATEGY_RICH, context));
        } else {
            scenario = null;
        }

        buildObjectType();

        // the field is used if the schemaStyle is 'SIMPLE_AND_RICH', but is ignored/unused otherwise
        setField(newFieldDefinition()
                .name(STRATEGY_RICH.topLevelFieldNameFrom(graphqlConfiguration))
                .type(getGqlObjectType())
                .build());
    }

    @Override
    protected void addDataFetchersForChildren() {

        super.addDataFetchersForChildren();

        if (scenario != null) {
            scenario.addDataFetcher(this);
        }
    }
}
