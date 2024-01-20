package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvDomainObjectMutators {

    private final GqlvDomainObject domainObject;

    final GraphQLObjectType.Builder mutatorsTypeBuilder;

    /**
     * Built lazily using {@link #buildMutatorsTypeIfAny()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> mutatorsTypeIfAny;

    public GqlvDomainObjectMutators(
            final GqlvDomainObject domainObject) {
        this.domainObject = domainObject;

        mutatorsTypeBuilder = newObject().name(this.domainObject.getLogicalTypeNameSanitized() + "__DomainObject_mutators");

    }

    public void addActionAsField(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition) {

        mutatorsTypeBuilder.field(fieldDefinition);
        actions.add(new GqlvAction(objectAction, fieldDefinition));
    }

    private final List<GqlvAction> actions = new ArrayList<>();
    public List<GqlvAction> getActions() {return Collections.unmodifiableList(actions);}

    boolean hasActions() {
        return !actions.isEmpty();
    }


    /**
     * @see #buildMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny == null) {
            throw new IllegalArgumentException(String.format("Gql MutatorsType has not yet been built for %s", domainObject.getLogicalTypeName()));
        }
        return mutatorsTypeIfAny;
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny != null) {
            throw new IllegalArgumentException("Gql MutatorsType has already been built for " + domainObject.getLogicalTypeName());
        }
        return mutatorsTypeIfAny = hasActions()
                ? Optional.of(mutatorsTypeBuilder.build())
                : Optional.empty();
    }

}
