package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;

import lombok.Getter;

public class GqlvTopLevelQuery implements GqlvDomainService.Holder {
    @Getter final GraphQLObjectType.Builder queryBuilder;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();


    /**
     * Built using {@link #buildQueryType()}
     */
    private GraphQLObjectType queryType;


    public GqlvTopLevelQuery() {
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

    public void addDomainService(ObjectSpecification objectSpec, Object servicePojo, Context context) {
        domainServices.add(new GqlvDomainService(this, objectSpec, servicePojo, context));
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates("Query", fieldDefinition.getName());
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        queryBuilder.field(field);
        return field;
    }

    public void addDataFetchers() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetchers();
            }
        });
    }


}
