package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLOutputType;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util._LTN;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvDomainService implements GqlvActionHolder, GqlvMutatorsHolder {

    @Getter private final ObjectSpecification objectSpecification;
    @Getter private final Object pojo;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GqlvMutators mutators;

    private String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }

    private final GraphQLObjectType.Builder objectTypeBuilder;

    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object pojo,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.objectSpecification = objectSpecification;
        this.pojo = pojo;
        this.codeRegistryBuilder = codeRegistryBuilder;

        this.mutators = new GqlvMutators(this, codeRegistryBuilder);

        this.objectTypeBuilder = newObject().name(_LTN.sanitized(objectSpecification));
    }


    private final List<GqlvAction> safeActions = new ArrayList<>();
    public List<GqlvAction> getSafeActions() {return Collections.unmodifiableList(safeActions);}

    private final List<GqlvAction> mutatorActions = new ArrayList<>();
    public List<GqlvAction> getMutatorActions() {return Collections.unmodifiableList(mutatorActions);}

    /**
     * @see #getGqlObjectType()
     */
    public GraphQLObjectType buildObjectGqlType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = objectTypeBuilder.build();
    }

    /**
     * @see #buildObjectGqlType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType;
    }

    public GraphQLFieldDefinition createTopLevelQueryField() {
        return newFieldDefinition()
                .name(_LTN.sanitized(objectSpecification))
                .type(objectTypeBuilder)
                .build();
    }


    public void addAction(final ObjectAction objectAction) {

        // TODO: either safe or mutator
        safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
    }



}
