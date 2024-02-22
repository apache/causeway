package org.apache.causeway.viewer.graphql.model.domain.common.query;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;

import lombok.Getter;
import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvTopLevelQuerySimpleSchema;

public abstract class GqlvTopLevelQueryAbstractSchema
        extends GqlvAbstractCustom
        implements Parent {

    @Getter private final SchemaStrategy schemaStrategy;

    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    public GqlvTopLevelQueryAbstractSchema(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super(schemaStrategy.getSchemaType().name() + "Schema", context);
        this.schemaStrategy = schemaStrategy;

        graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        // add domain object lookup to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(addChildFieldFor(GqlvTopLevelQuerySimpleSchema.of(schemaStrategy, objectSpec, context)));

                    break;
            }
        });

        // add services to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {
                case MANAGED_BEAN_CONTRIBUTING: // @DomainService
                    context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                            .ifPresent(servicePojo ->
                                    domainServices.add(
                                            addChildFieldFor(GqlvTopLevelQuerySimpleSchema.of(schemaStrategy, objectSpec, servicePojo, context))));
                    break;
            }
        });

    }

    protected static List<ObjectSpecification> superclassesOf(final ObjectSpecification objectSpecification) {
        val superclasses = new ArrayList<ObjectSpecification>();
        ObjectSpecification superclass = objectSpecification.superclass();
        while (superclass != null && superclass.getCorrespondingClass() != Object.class) {
            superclasses.add(0, superclass);
            superclass = superclass.superclass();
        }
        return superclasses;
    }

    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return environment;
    }

    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetcher(this);
            }
        });

        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));

    }
}
