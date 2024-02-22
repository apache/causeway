package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.common.SchemaStrategy;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.common.query.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.simple.query.GqlvTopLevelQuerySimpleSchema;

public class GqlvScenarioStep
        extends GqlvAbstractCustom
        implements Parent {

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    public GqlvScenarioStep(
            final SchemaStrategy schemaStrategy,
            final Context context) {
        super("ScenarioStep", context);

        if(isBuilt()) {
            return;
        }

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

        context.objectSpecifications().forEach(objectSpec -> {
            if (Objects.requireNonNull(objectSpec.getBeanSort()) == BeanSort.MANAGED_BEAN_CONTRIBUTING) { // @DomainService
                context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                        .ifPresent(servicePojo -> domainServices.add(addChildFieldFor(GqlvTopLevelQuerySimpleSchema.of(schemaStrategy, objectSpec, servicePojo, context))));
            }
        });

        buildObjectType();
    }


    protected void addDataFetchersForChildren() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetcher(this);
            }
        });

        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));
    }

    @Override
    protected DataFetchingEnvironment fetchData(DataFetchingEnvironment environment) {
        return environment;
    }

}
