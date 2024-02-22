package org.apache.causeway.viewer.graphql.model.domain.common.query;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstract;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;

import lombok.Getter;
import lombok.val;

public abstract class GqlvTopLevelQueryAbstractSchema
        extends GqlvAbstractCustom
        implements Parent {

    @Getter private final SchemaStrategy schemaStrategy;

    private final List<GqlvAbstract> domainServices = new ArrayList<>();
    private final List<GqlvAbstractCustom> domainObjects = new ArrayList<>();

    public GqlvTopLevelQueryAbstractSchema(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super(schemaStrategy.getSchemaType().name() + "Schema", context);
        this.schemaStrategy = schemaStrategy;

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    val gqlvDomainObject = schemaStrategy.domainObjectFor(objectSpec, context);
                    addChildField(gqlvDomainObject.newField());
                    domainObjects.add(gqlvDomainObject);
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
                                            addChildFieldFor(schemaStrategy.domainServiceFor(objectSpec, servicePojo, context))));
                    break;
            }
        });

    }

    public static List<ObjectSpecification> superclassesOf(final ObjectSpecification objectSpecification) {
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
        domainServices.forEach(domainService -> domainService.addDataFetcher(this));
        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));
    }
}
