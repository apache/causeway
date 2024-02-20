package org.apache.causeway.viewer.graphql.model.toplevel;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;

public class SimpleSchema extends GqlvAbstractCustom {

    protected SimpleSchema(final Context context) {
        super("simple", context);

    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }
}
