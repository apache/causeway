package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionParameterProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderActionParamDetails
        extends HolderActionParams,
        ObjectActionParameterProvider{

    HolderActionParams getHolder();

    void addGqlArgument(
            ObjectAction objectAction,
            GraphQLFieldDefinition.Builder fieldBuilder,
            TypeMapper.InputContext inputContext,
            int paramNum);
}
