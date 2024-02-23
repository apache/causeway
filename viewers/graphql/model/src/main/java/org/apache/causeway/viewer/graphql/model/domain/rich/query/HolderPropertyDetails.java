package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectMemberProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.ObjectSpecificationProvider;
import org.apache.causeway.viewer.graphql.model.mmproviders.SchemaTypeProvider;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

public interface HolderPropertyDetails
        extends HolderAssociationDetails<OneToOneAssociation> {

    void addGqlArgument(
            OneToOneAssociation otoa,
            GraphQLFieldDefinition.Builder fieldBuilder,
            TypeMapper.InputContext inputContext);

    default HolderMemberDetails<OneToOneAssociation> asHolderMemberDetails() {
        return new HolderMemberDetails<>() {
            @Override public OneToOneAssociation getObjectMember() {return HolderPropertyDetails.this.getObjectMember();}
            @Override public ObjectSpecification getObjectSpecification() {return HolderPropertyDetails.this.getObjectSpecification();}
            @Override public SchemaType getSchemaType() {return HolderPropertyDetails.this.getSchemaType();}
        };
    }
}
