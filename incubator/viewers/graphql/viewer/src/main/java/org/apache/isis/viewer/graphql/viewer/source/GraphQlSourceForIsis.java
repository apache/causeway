package org.apache.isis.viewer.graphql.viewer.source;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Service()
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class GraphQlSourceForIsis implements GraphQlSource {

    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;
    private final IsisConfiguration isisConfiguration;
    private final IsisSystemEnvironment isisSystemEnvironment;
    private final ExecutionStrategyResolvingWithinInteraction executionStrategy;
    private final ObjectTypeFactory objectTypeFactory;
    private final QueryFieldFactory queryFieldFactory;

    @PostConstruct
    public void init() {
        boolean fullyIntrospect = IntrospectionMode.isFullIntrospect(isisConfiguration, isisSystemEnvironment);
        if (!fullyIntrospect) {
            throw new IllegalStateException("GraphQL requires full introspection mode");
        }
    }

    @Override
    public GraphQL graphQl() {
        return GraphQL.newGraphQL(schema())
//                .instrumentation(new TracingInstrumentation())
                .queryExecutionStrategy(executionStrategy)
                .build();
    }

    @Override
    public GraphQLSchema schema() {

        val fullyIntrospected = specificationLoader.isMetamodelFullyIntrospected();
        if (!fullyIntrospected) {
            throw new IllegalStateException("Metamodel is not fully introspected");
        }

        val queryBuilder = newObject().name("Query");
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        Set<GraphQLType> graphQLObjectTypes = new HashSet<>();

        specificationLoader.forEach(objectSpecification -> {

            val logicalTypeName = objectSpecification.getLogicalTypeName();
            String logicalTypeNameSanitized = Utils.logicalTypeNameSanitized(logicalTypeName);

            switch (objectSpecification.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:    // @DomainObject(nature=ENTITY)

                    // TODO: App interface should mapp to gql interfaces?
                    objectTypeFactory.objectTypeFromObjectSpecification(objectSpecification, graphQLObjectTypes, codeRegistryBuilder);

                    break;

                case MANAGED_BEAN_CONTRIBUTING: //@DomainService

                    queryFieldFactory.queryFieldFromObjectSpecification(queryBuilder, codeRegistryBuilder, objectSpecification);
                    break;

                case MANAGED_BEAN_NOT_CONTRIBUTING: // a @Service or @Component ... ignore
                case MIXIN:
                case VALUE:
                case COLLECTION:
                case VETOED:
                case UNKNOWN:
                    break;
            }
        }, false);

        val query_numServices = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();

        GraphQLObjectType query = queryBuilder
                .field(query_numServices)
                .build();


        val codeRegistry = codeRegistryBuilder
                .dataFetcher(coordinates(query.getName(), query_numServices.getName()),
                        (DataFetcher<Object>) environment -> this.serviceRegistry.streamRegisteredBeans().count())
                .build();


        return GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(graphQLObjectTypes)
                .codeRegistry(codeRegistry)
                .build();
    }

}
