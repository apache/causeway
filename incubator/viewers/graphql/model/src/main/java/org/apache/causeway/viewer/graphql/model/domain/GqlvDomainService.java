package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types._Constants;
import org.apache.causeway.viewer.graphql.model.util._LTN;

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
    public GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = objectTypeBuilder.build();
    }

    /**
     * @see #buildGqlObjectType()
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
        if (objectAction.getSemantics().isSafeInNature()) {
            safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
        } else {
            // TODO: should register with mutators instead ...
//            mutators.addAction(objectAction);
            safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
        }
    }

    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        return mutators.buildMutatorsTypeIfAny();
    }

    /**
     * @return <code>true</code> if any (at least one) actions were added
     */
    public boolean addActions() {

        List<ObjectAction> objectActionList = objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .collect(Collectors.toList());

        objectActionList.forEach(this::addAction);

        Optional<GraphQLObjectType> mutatorsTypeIfAny = buildMutatorsTypeIfAny();
        mutatorsTypeIfAny.ifPresent(mutatorsType -> {
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Constants.GQL_MUTATIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);
        });

        return !objectActionList.isEmpty();
    }

    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {

        GraphQLObjectType graphQLObjectType = buildGqlObjectType();
        //graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);

        getMutatorsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        return mutators.getMutatorsTypeIfAny();
    }

}
