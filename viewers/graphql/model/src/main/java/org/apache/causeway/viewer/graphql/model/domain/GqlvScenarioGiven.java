package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.viewer.graphql.model.context.Context;

public class GqlvScenarioGiven
        extends GqlvAbstractCustom
        implements Parent {

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    public GqlvScenarioGiven(
            final Context context) {
        super("Given", context);

        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(GqlvDomainObject.of(objectSpec, context));

                    break;
            }
        });

        context.objectSpecifications().forEach(objectSpec -> {
            if (Objects.requireNonNull(objectSpec.getBeanSort()) == BeanSort.MANAGED_BEAN_CONTRIBUTING) { // @DomainService
                context.serviceRegistry.lookupBeanById(objectSpec.getLogicalTypeName())
                        .ifPresent(servicePojo -> {
                            GqlvDomainService gqlvDomainService = GqlvDomainService.of(objectSpec, servicePojo, context);
                            addChildField(gqlvDomainService.getField());
                            domainServices.add(gqlvDomainService);
                        });
            }
        });

        // add domain object lookup to top-level query
        for (GqlvDomainObject domainObject : this.domainObjects) {
            addChildField(domainObject.getLookupField());
        }


        buildObjectTypeAndField("Given");
    }


    public void addDataFetchers(Parent parent) {
        context.codeRegistryBuilder.dataFetcher(
                parent.coordinatesFor(getField()),
                this::fetchData);

        addDataFetchersForChildren();
    }

    protected void addDataFetchersForChildren() {
        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetchers(this);
            }
        });

        domainObjects.forEach(domainObject -> domainObject.addDataFetchers(this));
    }

    @Override
    protected DataFetchingEnvironment fetchData(DataFetchingEnvironment environment) {
        return environment;
    }


}
