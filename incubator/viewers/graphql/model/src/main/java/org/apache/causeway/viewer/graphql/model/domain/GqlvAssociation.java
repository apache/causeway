package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.FieldCoordinates.coordinates;

public abstract class GqlvAssociation<T extends ObjectAssociation, H extends GqlvAssociationHolder> extends GqlvMember<T, H> {


    private final SpecificationLoader specificationLoader;

    public GqlvAssociation(
            final H holder,
            final T objectAssociation,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final SpecificationLoader specificationLoader
            ) {
        super(holder, objectAssociation, fieldDefinition, codeRegistryBuilder);
        this.specificationLoader = specificationLoader;
    }

    /**
     * @see #getObjectMember()
     */
    public T getObjectAssociation() {
        return getObjectMember();
    }

    public void addDataFetcher() {

        final ObjectAssociation association = getObjectMember();
        final GraphQLFieldDefinition field = getFieldDefinition();

        final GraphQLObjectType graphQLObjectType = getHolder().getGqlObjectType();

        ObjectSpecification fieldObjectSpecification = association.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VALUE: //TODO: does this work for values as well?

            case VIEW_MODEL:

            case ENTITY:

                codeRegistryBuilder.dataFetcher(
                        coordinates(graphQLObjectType, field),
                        (DataFetcher<Object>) environment -> {

                            Object domainObjectInstance = environment.getSource();

                            Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                            ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                            ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);
                            ManagedObject managedObject = association.get(owner);

                            return managedObject!=null ? managedObject.getPojo() : null;
                        });

                break;

        }
    }

}
