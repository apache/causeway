package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import lombok.Getter;
import lombok.val;

public class GqlvTopLevelQuery implements GqlvDomainService.Holder {

    final GraphQLObjectType.Builder objectTypeBuilder;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final Context context;
    private final List<GqlvDomainObject> domainObjects;


    @Getter private final GraphQLObjectType objectType;


    public GqlvTopLevelQuery(
            final Context context,
            final List<GqlvDomainObject> domainObjects) {
        this.context = context;
        this.domainObjects = domainObjects;
        this.objectTypeBuilder = newObject().name("Query");

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo -> {
                                addDomainService(objectSpec, servicePojo, context);
                            });
                    break;
            }
        });

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo -> {
                                addDataFetchers();
                            });
                    break;
            }
        });

        // add lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addField(domainObject.getField());
        }


        objectType = objectTypeBuilder.build();
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
        objectTypeBuilder.field(field);
        return field;
    }

    public void addDataFetchers() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetchers();
            }
        });

        domainObjects.forEach(domainObject -> {
            ObjectSpecification objectSpec = domainObject.getObjectSpecification();
            this.context.codeRegistryBuilder.dataFetcher(
                    coordinatesFor(domainObject.getField()),
                    (DataFetcher<Object>) environment -> {
                        Object target = environment.getArgument("object");
                        return GqlvAction.asPojo(objectSpec, target, this.context.bookmarkService)
                                .orElse(null);
                    });
        });

    }


}
