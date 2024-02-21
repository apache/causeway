package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.query.GqlvTopLevelRichSchema;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvTopLevelSimpleSchema;

public class GqlvTopLevelForSimpleAndRich extends GqlvAbstractCustom {

    private final GqlvTopLevelRichSchema richSchema;
    private final GqlvTopLevelSimpleSchema simpleSchema;

    public GqlvTopLevelForSimpleAndRich(final Context context) {
        super("SimpleAndRich", context);

        addChildFieldFor(richSchema = new GqlvTopLevelRichSchema(context));
        addChildFieldFor(simpleSchema = new GqlvTopLevelSimpleSchema(context));

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
