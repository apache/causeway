package org.apache.causeway.viewer.graphql.model.domain.rich;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.GqlvScenario;
import org.apache.causeway.viewer.graphql.model.domain.Parent;

public class GqlvTopLevelRich
        extends GqlvAbstractCustom
        implements Parent {

    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    private final List<GqlvDomainService> domainServices = new ArrayList<>();
    private final List<GqlvDomainObject> domainObjects = new ArrayList<>();

    private final GqlvScenario scenario;

    public GqlvTopLevelRich(final Context context) {
        super("Query", context);

        graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

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

        if (graphqlConfiguration.isIncludeTestingFieldInRich()) {
            addChildFieldFor(scenario = new GqlvScenario(context));
        } else {
            scenario = null;
        }

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

        if (scenario != null) {
            scenario.addDataFetcher(this);
        }
    }
}
