package org.apache.causeway.viewer.graphql.model.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.rich.GqlvDomainObject;
import org.apache.causeway.viewer.graphql.model.domain.rich.GqlvDomainService;
import org.apache.causeway.viewer.graphql.model.domain.GqlvScenario;
import org.apache.causeway.viewer.graphql.model.domain.Parent;

public class GqlvTopLevelQuery
        extends GqlvAbstractCustom
        implements Parent {

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    private final GqlvScenario scenario;

    public GqlvTopLevelQuery(final Context context) {
        super("Query", context);

        // add domain object lookup to top-level query
        context.objectSpecifications().forEach(objectSpec -> {
            switch (objectSpec.getBeanSort()) {

                case ABSTRACT:
                case VIEW_MODEL: // @DomainObject(nature=VIEW_MODEL)
                case ENTITY:     // @DomainObject(nature=ENTITY)

                    domainObjects.add(addChildFieldFor(GqlvDomainObject.of(objectSpec, context)));

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
                                            addChildFieldFor(GqlvDomainService.of(objectSpec, servicePojo, context))));
                    break;
            }
        });

        addChildFieldFor(scenario = new GqlvScenario(context));

        buildObjectType();
    }

    /**
     * Never used.
     *
     * @param environment
     * @return
     */
    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return null;
    }

    @Override
    public GraphQLObjectType getGqlObjectType() {
        return super.getGqlObjectType();
    }

    public void addDataFetchers() {

        domainServices.forEach(domainService -> {
            boolean actionsAdded = domainService.hasActions();
            if (actionsAdded) {
                domainService.addDataFetcher(this);
            }
        });


        domainObjects.forEach(domainObject -> domainObject.addDataFetcher(this));

        scenario.addDataFetcher(this);
    }
}
