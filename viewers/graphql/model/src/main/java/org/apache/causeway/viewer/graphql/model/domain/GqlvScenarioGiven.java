package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.viewer.graphql.model.context.Context;

import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvScenarioGiven implements GqlvDomainService.Holder, GqlvDomainObject.Holder {

    private static final String OBJECT_TYPE_NAME = "Given";

    final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    @Getter private final GraphQLObjectType objectType;

    private final Holder holder;
    @Getter private final GraphQLFieldDefinition field;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    public GqlvScenarioGiven(
            final GqlvScenarioGiven.Holder holder,
            final Context context) {

        this.holder = holder;

        this.gqlObjectTypeBuilder = newObject().name(OBJECT_TYPE_NAME);

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(GqlvDomainObject.of(objectSpec, this, context));

                    break;
            }
        });

        context.objectSpecifications().forEach(objectSpec -> {
            if (Objects.requireNonNull(objectSpec.getBeanSort()) == BeanSort.MANAGED_BEAN_CONTRIBUTING) { // @DomainService
                context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                        .ifPresent(servicePojo -> {
                            GqlvDomainService gqlvDomainService = GqlvDomainService.of(objectSpec, this, servicePojo, context);
                            addField(gqlvDomainService.getField());
                            domainServices.add(gqlvDomainService);
                        });
            }
        });

        // add domain object lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addField(domainObject.getLookupField());
        }


        objectType = gqlObjectTypeBuilder.build();

        this.field = GraphQLFieldDefinition.newFieldDefinition().name("Given").type(objectType).build();
    }


    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(objectType, fieldDefinition);
    }

    private GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        if (field != null) {
            gqlObjectTypeBuilder.field(field);
        }
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
    }

    public interface Holder
            extends GqlvHolder {
    }

}
