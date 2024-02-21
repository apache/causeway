package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvTopLevelQueryRichSchema;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvTopLevelQuerySimpleSchema;

public class GqlvTopLevelQueryBothSchemas extends GqlvAbstractCustom {

    private final GqlvTopLevelQueryRichSchema richSchema;
    private final GqlvTopLevelQuerySimpleSchema simpleSchema;

    public GqlvTopLevelQueryBothSchemas(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new GqlvTopLevelQueryRichSchema(context));
        addChildFieldFor(simpleSchema = new GqlvTopLevelQuerySimpleSchema(context));

        buildObjectType();
    }

    @Override
    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        richSchema.addDataFetcher(this);
        simpleSchema.addDataFetcher(this);
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
