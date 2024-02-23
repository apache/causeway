package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderPropertyDetails
        extends SchemaTypeProvider,
                ObjectSpecificationProvider,
                ObjectMemberProvider<OneToOneAssociation> {

    void addGqlArgument(
            OneToOneAssociation otoa,
            GraphQLFieldDefinition.Builder fieldBuilder,
            TypeMapper.InputContext inputContext);
}
