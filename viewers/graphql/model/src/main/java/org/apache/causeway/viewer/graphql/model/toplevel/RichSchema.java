package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;

public class RichSchema extends GqlvAbstractCustom {

    protected RichSchema(final Context context) {
        super("rich", context);

    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
