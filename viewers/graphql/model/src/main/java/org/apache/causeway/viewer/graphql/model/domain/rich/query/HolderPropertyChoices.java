package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.OneToOneAssociationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderPropertyChoices
        extends ObjectSpecificationProvider,
        OneToOneAssociationProvider,
        SchemaTypeProvider {

    void addGqlArgument(OneToOneAssociation otoa, GraphQLFieldDefinition.Builder fieldBuilder, TypeMapper.InputContext inputContext);
}
