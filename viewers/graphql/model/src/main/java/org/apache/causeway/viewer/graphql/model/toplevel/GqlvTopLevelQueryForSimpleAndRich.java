package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;

public class GqlvTopLevelQueryForSimpleAndRich extends GqlvAbstractCustom {

    private final RichSchema richSchema;
    private final SimpleSchema simpleSchema;

    public GqlvTopLevelQueryForSimpleAndRich(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new RichSchema(context));
        addChildFieldFor(simpleSchema = new SimpleSchema(context));

        buildObjectType();
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
