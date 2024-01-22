package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

public abstract class GqlvAssociation<T extends ObjectAssociation, H extends GqlvAssociationHolder> extends GqlvMember<T, H> {

    public GqlvAssociation(
            final H holder,
            final T objectAssociation,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
            ) {
        super(holder, objectAssociation, fieldDefinition, codeRegistryBuilder);
    }

    public boolean hasFieldDefinition() {
        return getFieldDefinition() != null;
    }

    /**
     * @see #getObjectMember()
     */
    public T getObjectAssociation() {
        return getObjectMember();
    }

    public void addDataFetcher() {

        final ObjectAssociation association = getObjectAssociation();
        final ObjectSpecification fieldObjectSpecification = association.getElementType();
        final BeanSort beanSort = fieldObjectSpecification.getBeanSort();

        switch (beanSort) {

            case VALUE:
            case VIEW_MODEL:
            case ENTITY:

                codeRegistryBuilder.dataFetcher(
                        getHolder().coordinatesFor(getFieldDefinition()),
                        (DataFetcher<Object>) environment -> {

                            Object domainObjectInstance = environment.getSource();

                            Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                            ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);
                            if (specification == null) {
                                return null;
                            }

                            // TODO: probably incorrect to adapt as a singular here.
                            ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);
                            ManagedObject managedObject = association.get(owner);

                            return managedObject!=null ? managedObject.getPojo() : null;
                        });

                break;

        }
    }

}
