package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;

import lombok.Getter;
import lombok.val;

public class GqlvTopLevelQuery implements GqlvDomainService.Holder, GqlvDomainObject.Holder {

    private static final String OBJECT_TYPE_NAME = "Query";

    final GraphQLObjectType.Builder objectTypeBuilder;
    @Getter private final GraphQLObjectType objectType;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();
    private final Context context;

    public GqlvTopLevelQuery(
            final Context context,
            final ObjectManager objectManager,
            final GraphQLTypeRegistry graphQLTypeRegistry) {

        this.context = context;
        this.objectTypeBuilder = newObject().name(OBJECT_TYPE_NAME);

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(new GqlvDomainObject(this, objectSpec, context, objectManager, graphQLTypeRegistry));

                    break;
            }
        });

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

        // add lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addField(domainObject.getLookupField());
        }

        objectType = objectTypeBuilder.build();
    }


    public void addDomainService(ObjectSpecification objectSpec, Object servicePojo, Context context) {
        domainServices.add(new GqlvDomainService(this, objectSpec, servicePojo, context));
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

    }


}
