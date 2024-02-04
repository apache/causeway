package org.apache.causeway.viewer.graphql.viewer.toplevel;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import lombok.Getter;
import lombok.val;

public class GqlvTopLevelQuery {

    private final ServiceRegistry serviceRegistry;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter final GraphQLObjectType.Builder queryBuilder;


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
        queryBuilder.field(topLevelQueryField);

        codeRegistryBuilder.dataFetcher(
                // TODO: it would be nice to make these typesafe...
                FieldCoordinates.coordinates("Query", topLevelQueryField.getName()),
                (DataFetcher<Object>) environment -> domainService.getServicePojo());

    }

    public void addDomainServiceTo(final ObjectSpecification objectSpec, final Object servicePojo, final Context context) {
        val domainService = new GqlvDomainService(objectSpec, servicePojo, context);

        boolean actionsAdded = domainService.hasActions();
        if (actionsAdded) {
            addFieldFor(domainService, context.codeRegistryBuilder);
        }
    }

}
