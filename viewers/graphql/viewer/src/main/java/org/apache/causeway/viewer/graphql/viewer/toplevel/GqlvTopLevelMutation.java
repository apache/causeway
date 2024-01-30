package org.apache.causeway.viewer.graphql.viewer.toplevel;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;

import lombok.Getter;

public class GqlvTopLevelMutation {

    private final ServiceRegistry serviceRegistry;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter final GraphQLObjectType.Builder gqlObjectBuilder;

    @Getter private GraphQLFieldDefinition numServicesField;

    /**
     * Built using {@link #buildMutationType()}
     */
    private GraphQLObjectType objectType;


    public GqlvTopLevelMutation(
            final ServiceRegistry serviceRegistry,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.serviceRegistry = serviceRegistry;
        this.codeRegistryBuilder = codeRegistryBuilder;
        gqlObjectBuilder = newObject().name("Mutation");

        numServicesField = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();
        gqlObjectBuilder.field(numServicesField);
    }



    public GraphQLObjectType buildMutationType() {
        if (objectType != null) {
            throw new IllegalStateException("Mutation type has already been built");
        }
        return objectType = gqlObjectBuilder.build();
    }

    /**
     *
     * @see #buildMutationType()
     */
    public GraphQLObjectType getObjectType() {
        if (objectType == null) {
            throw new IllegalStateException("Mutation type has not yet been built");
        }
        return objectType;
    }

//    public void addFieldFor(
//            final GqlvDomainService domainService,
//            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
//
//        GraphQLFieldDefinition topLevelQueryField = domainService.createTopLevelQueryField();
//        gqlObjectBuilder.field(topLevelQueryField);
//
//        codeRegistryBuilder.dataFetcher(
//                // TODO: it would be nice to make these typesafe...
//                FieldCoordinates.coordinates("Mutation", topLevelQueryField.getName()),
//                (DataFetcher<Object>) environment -> domainService.getServicePojo());
//
//    }

    public void addFetchers() {
        codeRegistryBuilder
            .dataFetcher(
                coordinates(getObjectType(), getNumServicesField()),
                (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count());
    }

}
