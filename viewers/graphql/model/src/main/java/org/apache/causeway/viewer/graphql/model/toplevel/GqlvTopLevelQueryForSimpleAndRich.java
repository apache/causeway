package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.GqlvTopLevelRichSchema;

public class GqlvTopLevelQueryForSimpleAndRich extends GqlvAbstractCustom {

    private final GqlvTopLevelRichSchema richSchema;

    public GqlvTopLevelQueryForSimpleAndRich(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new GqlvTopLevelRichSchema(context));

        buildObjectType();
    }

    @Override
    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        richSchema.addDataFetcher(this);
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
