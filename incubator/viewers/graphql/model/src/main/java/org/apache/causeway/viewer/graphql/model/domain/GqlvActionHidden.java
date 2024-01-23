package org.apache.causeway.viewer.graphql.model.domain;

import java.util.Map;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;

import lombok.extern.log4j.Log4j2;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Log4j2
public class GqlvActionHidden {

    private final GqlvActionHiddenHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final GraphQLFieldDefinition field;

    public GqlvActionHidden(
            final GqlvActionHiddenHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.field = fieldDefinition(holder);
    }

    private static GraphQLFieldDefinition fieldDefinition(final GqlvActionHiddenHolder holder) {

        GraphQLFieldDefinition fieldDefinition =
                newFieldDefinition()
                    .name("hidden")
                    .type(ScalarMapper.typeFor(boolean.class))
                    .build();

        holder.addField(fieldDefinition);
        return fieldDefinition;
    }

    public void addDataFetcher() {
        codeRegistryBuilder.dataFetcher(
                holder.coordinatesFor(field),
                this::hidden
        );
    }

    private boolean hidden(
            final DataFetchingEnvironment dataFetchingEnvironment) {

        final ObjectAction objectAction = holder.getObjectAction();

        Object source = dataFetchingEnvironment.getSource();
        Object domainObjectInstance;
        if (source instanceof GqlvAction.Fetcher) {
            GqlvAction.Fetcher fetcher = (GqlvAction.Fetcher) source;
            domainObjectInstance = fetcher.getTargetPojo();
        } else {
            domainObjectInstance = source;
        }

        Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
        ObjectSpecification specification = holder.getObjectAction().getSpecificationLoader()
                .loadSpecification(domainObjectInstanceClass);
        if (specification == null) {
            // not expected
            return true;
        }

        ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

        val visibleConsent = objectAction.isVisible(owner, InteractionInitiatedBy.USER, Where.ANYWHERE);
        return visibleConsent.isVetoed();
    }

}
