package org.apache.causeway.viewer.graphql.viewer.source;


import java.util.Set;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import static graphql.schema.GraphQLObjectType.newObject;

@RequiredArgsConstructor
public class GqlvObjectBehaviour {

    private final GqlvObjectStructure gqlvObjectStructure;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final SpecificationLoader specificationLoader;



    public void createAndRegisterDataFetchersForMetaData() {

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(gqlvObjectStructure.getGqlObjectType(), gqlvObjectStructure.getMetaField()),
                (DataFetcher<Object>) environment -> {
                    return bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new GqlvMeta(bookmark, bookmarkService, objectManager))
                            .orElse(null); //TODO: is this correct ?
                });

        GraphQLObjectType metaType = gqlvObjectStructure.getMetaType();
        gqlvObjectStructure.getMetaField().getType();
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(metaType, GqlvObjectStructure.Fields.id),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.id();
                });

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(gqlvObjectStructure.getMetaType(), GqlvObjectStructure.Fields.logicalTypeName),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.logicalTypeName();
                });

        if (gqlvObjectStructure.getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates(gqlvObjectStructure.getMetaType(), GqlvObjectStructure.Fields.version),
                    (DataFetcher<Object>) environment -> {
                        GqlvMeta gqlvMeta = environment.getSource();
                        return gqlvMeta.version();
                    });
        }
    }


    public void createAndRegisterDataFetchersForField() {
        gqlvObjectStructure.getObjectSpec().streamProperties(MixedIn.INCLUDED)
                .forEach(this::createAndRegisterDataFetcherForObjectAssociation);
    }

    void createAndRegisterDataFetchersForCollection() {
        gqlvObjectStructure.getObjectSpec().streamCollections(MixedIn.INCLUDED)
                .forEach(this::createAndRegisterDataFetcherForObjectAssociation);
    }


    private void createAndRegisterDataFetcherForObjectAssociation(final ObjectAssociation otom) {

        final GraphQLObjectType graphQLObjectType = gqlvObjectStructure.getGqlObjectType();

        ObjectSpecification fieldObjectSpecification = otom.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VALUE: //TODO: does this work for values as well?

            case VIEW_MODEL:

            case ENTITY:

                codeRegistryBuilder
                        .dataFetcher(
                                FieldCoordinates.coordinates(graphQLObjectType, otom.getId()),
                                (DataFetcher<Object>) environment -> {

                                    Object domainObjectInstance = environment.getSource();

                                    Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                                    ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                                    ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

                                    ManagedObject managedObject = otom.get(owner);

                                    return managedObject!=null ? managedObject.getPojo() : null;

                                });


                break;

        }
    }

    public void createAndRegisterDataFetchersForMutators() {

        // something like:

//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLTypeReference, gql_mutations), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
//                    if (bookmark == null) return null; //TODO: is this correct ?
//                    return new GqlvMutations(bookmark, bookmarkService, mutatorsTypeFields);
//                }
//            });
//
//            // for each field something like
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(mutatorsType, idField), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    GqlvMeta gqlMeta = environment.getSource();
//
//                    return gqlMeta.id();
//                }
//            });


    }


    GraphQLObjectType createAndRegisterMutatorsType(
            final Set<GraphQLType> graphQLObjectTypes) {

        //TODO: this is not going to work, because we need to dynamically add fields
        String mutatorsTypeName = gqlvObjectStructure.getLogicalTypeNameSanitized() + "__DomainObject_mutators";
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
        graphQLObjectTypes.add(mutatorsType);
        return mutatorsType;
    }

}
