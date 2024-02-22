package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectActionProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderActionValidity
        extends ObjectSpecificationProvider,
        ObjectActionProvider,
        SchemaTypeProvider {

    void addGqlArguments(
            ObjectAction objectAction,
            GraphQLFieldDefinition.Builder fieldBuilder,
            TypeMapper.InputContext inputContext,
            int parameterCount);
}
