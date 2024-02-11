package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
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


    @Getter private final GraphQLObjectType queryType;


    public GqlvTopLevelQuery(
            final Context context,
            final Map<ObjectSpecification, GqlvDomainObject> domainObjects) {
        this.context = context;
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
        domainObjects.forEach(this::addLookupFor);

        queryType = objectTypeBuilder.build();
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
    }

    public void addLookupFor(
            final ObjectSpecification objectSpec,
            final GqlvDomainObject domainObject) {
        val lookupConfig = context.causewayConfiguration.getViewer().getGraphql().getLookup();
        val field = newFieldDefinition()
                        .name(String.format("%s%s%s",
                                lookupConfig.getFieldNamePrefix(),          // eg "_gqlv_lookup__"
                                TypeNames.objectTypeNameFor(objectSpec),
                                lookupConfig.getFieldNameSuffix())          // eg ""
                        )
                        .type(context.typeMapper.outputTypeFor(objectSpec))
                        .argument(GraphQLArgument.newArgument()
                                        .name(lookupConfig.getArgument())   // eg "object"
                                        .type(domainObject.getGqlInputObjectType())
                                        .build())
                        .build();
        addField(field);

        context.codeRegistryBuilder.dataFetcher(
                coordinatesFor(field),
                (DataFetcher<Object>) environment -> lookup(objectSpec, environment));

    }

    private Object lookup(ObjectSpecification objectSpec, DataFetchingEnvironment dataFetchingEnvironment) {
        Object target = dataFetchingEnvironment.getArgument("object");
        return GqlvAction.asPojo(objectSpec, target, context.bookmarkService)
                .orElse(null);
    }


}
