package org.apache.causeway.viewer.graphql.viewer.toplevel;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvTopLevelQuery {

    private final ServiceRegistry serviceRegistry;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter final GraphQLObjectType.Builder queryBuilder;

    @Getter private GraphQLFieldDefinition numServicesField;

    /**
     * Built using {@link #buildQueryType()}
     */
    private GraphQLObjectType queryType;


    public GqlvTopLevelQuery(
            final ServiceRegistry serviceRegistry,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        this.serviceRegistry = serviceRegistry;
        this.codeRegistryBuilder = codeRegistryBuilder;
        queryBuilder = newObject().name("Query");

        numServicesField = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();
        queryBuilder.field(numServicesField);
    }



    public GraphQLObjectType buildQueryType() {
        if (queryType != null) {
            throw new IllegalStateException("QueryType has already been built");
        }
        return queryType = queryBuilder.build();
    }

    /**
     *
     * @see #buildQueryType()
     */
    public GraphQLObjectType getQueryType() {
        if (queryType == null) {
            throw new IllegalStateException("QueryType has not yet been built");
        }
        return queryType;
    }

    public void addFieldFor(
            final GqlvDomainService domainService,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        GraphQLFieldDefinition topLevelQueryField = domainService.createTopLevelQueryField();
        queryBuilder.field(domainService.createTopLevelQueryField());

        codeRegistryBuilder.dataFetcher(
                // TODO: it would be nice to make these typesafe...
                FieldCoordinates.coordinates("Query", topLevelQueryField.getName()),
                (DataFetcher<Object>) environment -> domainService.getServicePojo());

    }

    public void addFetchers() {
        codeRegistryBuilder
                .dataFetcher(
                        coordinates(getQueryType(), getNumServicesField()),
                        (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count());
    }

}
