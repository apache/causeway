package org.apache.causeway.viewer.graphql.model.domain;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

public class GqlvProperty extends GqlvAssociation<OneToOneAssociation, GqlvPropertyHolder> {

    public GqlvProperty(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(domainObject, oneToOneAssociation, fieldDefinition(domainObject, oneToOneAssociation), codeRegistryBuilder);
    }

    @Nullable private static GraphQLFieldDefinition fieldDefinition(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation otoa) {

        GraphQLOutputType type = outputTypeFor(otoa);

        GraphQLFieldDefinition fieldDefinition = null;
        if (type != null) {
            fieldDefinition = newFieldDefinition()
                    .name(otoa.getId())
                    .type(type).build();
            domainObject.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    private static GraphQLOutputType outputTypeFor(final OneToOneAssociation otoa) {
        ObjectSpecification otoaObjectSpec = otoa.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec));
                return otoa.isOptional()
                        ? fieldTypeRef
                        : nonNull(fieldTypeRef);

            case VALUE:

                // todo: map ...

                return otoa.isOptional()
                        ? Scalars.GraphQLString
                        : nonNull(Scalars.GraphQLString);
        }
        return null;
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }

}
