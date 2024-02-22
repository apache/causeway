package org.apache.causeway.viewer.graphql.model.domain.simple.query;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvTopLevelQueryAbstractSchema;

import lombok.val;

public class GqlvTopLevelQuerySimpleSchema
        extends GqlvTopLevelQueryAbstractSchema {

    private static final SchemaStrategy SCHEMA_STRATEGY = SchemaStrategy.SIMPLE;

    public GqlvTopLevelQuerySimpleSchema(final Context context) {
        super(SCHEMA_STRATEGY, context);

        var graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        buildObjectType();

        // the field is used if the schemaStyle is 'SIMPLE_AND_RICH', but is ignored/unused otherwise
        setField(newFieldDefinition()
                .name(SCHEMA_STRATEGY.topLevelFieldNameFrom(graphqlConfiguration))
                .type(getGqlObjectType())
                .build());
    }

    public static GqlvDomainObject of(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification objectSpecification,
            final Context context) {

        mapSuperclassesIfNecessary(schemaStrategy, objectSpecification, context);
        return computeIfAbsentGqlvDomainObject(schemaStrategy, objectSpecification, context);
    }

    private static void mapSuperclassesIfNecessary(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification objectSpecification,
            final Context context) {
        // no need to map if the target subclass has already been built
        if(schemaStrategy.domainObjectBySpec(context).containsKey(objectSpecification)) {
            return;
        }
        val superclasses = superclassesOf(objectSpecification);
        superclasses.forEach(objectSpec -> computeIfAbsentGqlvDomainObject(schemaStrategy, objectSpec, context));
    }

    private static GqlvDomainObject computeIfAbsentGqlvDomainObject(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification objectSpec,
            final Context context) {
        return schemaStrategy.domainObjectBySpec(context).computeIfAbsent(objectSpec, spec -> new GqlvDomainObject(schemaStrategy, spec, context));
    }


    public static GqlvDomainService of(
            final SchemaStrategy schemaStrategy,
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final Context context) {
        return schemaStrategy.domainServiceBySpec(context).computeIfAbsent(objectSpecification, spec -> new GqlvDomainService(schemaStrategy, spec, servicePojo, context));
    }

}
