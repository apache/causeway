package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLObjectType;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Log4j2
public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder> implements GqlvActionInvokeHolder {

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType gqlObjectType;
    private final GqlvActionInvoke invoke;

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
            ) {
        super(holder, objectAction, codeRegistryBuilder);

        gqlObjectTypeBuilder = newObject().name(TypeNames.invokeTypeNameFor(objectAction));

        this.invoke = new GqlvActionInvoke(this, codeRegistryBuilder);

        gqlObjectType = gqlObjectTypeBuilder.build();

        final GraphQLFieldDefinition field = newFieldDefinition()
                .name(objectAction.getId())
                .type(gqlObjectTypeBuilder)
                .build();

        holder.addField(field);

        setFieldDefinition(field);
    }


    public ObjectAction getObjectAction() {
        return getObjectMember();
    }

    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

}
