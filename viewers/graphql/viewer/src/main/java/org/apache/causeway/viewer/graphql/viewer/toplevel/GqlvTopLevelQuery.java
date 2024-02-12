package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.GqlvScenario;

import lombok.Getter;

public class GqlvTopLevelQuery implements GqlvDomainService.Holder, GqlvDomainObject.Holder, GqlvScenario.Holder {

    private static final String OBJECT_TYPE_NAME = "Query";

    final GraphQLObjectType.Builder objectTypeBuilder;
    @Getter private final GraphQLObjectType objectType;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    private final GqlvScenario scenario;

    public GqlvTopLevelQuery(final Context context) {

        this.objectTypeBuilder = newObject().name(OBJECT_TYPE_NAME);

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(GqlvDomainObject.of(objectSpec, this, context));

                    break;
            }
        });

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo -> {
                                domainServices.add(GqlvDomainService.of(objectSpec, this, servicePojo, context));
                            });
                    break;
            }
        });

        // add domain object lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addField(domainObject.getLookupField());
        }

        scenario = new GqlvScenario(this, context);

        objectType = objectTypeBuilder.build();
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(OBJECT_TYPE_NAME, fieldDefinition.getName());
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


        domainObjects.forEach(GqlvDomainObject::addDataFetchers);

        scenario.addDataFetchers();
    }

}
