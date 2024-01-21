package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types._Constants;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import lombok.Getter;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvDomainService implements GqlvActionHolder, GqlvMutatorsHolder {

    @Getter private final ObjectSpecification objectSpecification;
    @Getter private final Object servicePojo;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter private final GqlvMutators mutators;
    private final GraphQLObjectType.Builder objectTypeBuilder;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }
    public String getLogicalTypeNameSanitized() {
        return TypeNames.objectTypeNameFor(objectSpecification);
    }


    private final List<GqlvAction> safeActions = new ArrayList<>();
    public List<GqlvAction> getSafeActions() {return Collections.unmodifiableList(safeActions);}

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.objectSpecification = objectSpecification;
        this.servicePojo = servicePojo;
        this.codeRegistryBuilder = codeRegistryBuilder;

        this.objectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.mutators = new GqlvMutators(this, codeRegistryBuilder);

    }

    /**
     * @return <code>true</code> if any (at least one) actions were added
     */
    public boolean addActions() {

        val anyActions = new AtomicBoolean(false);
        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {
                    anyActions.set(true);
                    addAction(objectAction);
                });

        Optional<GraphQLObjectType> mutatorsTypeIfAny = buildMutatorsTypeIfAny();
        mutatorsTypeIfAny.ifPresent(mutatorsType -> {
            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Constants.GQL_MUTATIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            objectTypeBuilder.field(gql_mutations);
        });

        return anyActions.get();
    }

    void addAction(final ObjectAction objectAction) {
        if (objectAction.getSemantics().isSafeInNature()) {
            safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
        } else {
             // TODO: should register with mutators instead ...
//            mutators.addAction(objectAction);
            safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
        }
    }


    /**
     * Should be called only after fields etc have been added.
     *
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


    /**
     * @see #buildMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        return mutators.getMutatorsTypeIfAny();
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        return mutators.buildMutatorsTypeIfAny();
    }


    public GraphQLFieldDefinition createTopLevelQueryField() {
        return newFieldDefinition()
                .name(TypeNames.objectTypeNameFor(objectSpecification))
                .type(objectTypeBuilder)
                .build();
    }



    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {

        GraphQLObjectType graphQLObjectType = buildGqlObjectType();
        //graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);

        getMutatorsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

}
