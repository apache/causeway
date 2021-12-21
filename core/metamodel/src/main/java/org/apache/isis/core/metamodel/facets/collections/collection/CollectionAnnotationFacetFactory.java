/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.facets.collections.collection;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.commons.internal.base._Optionals;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.reflection._Generics;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacet.Contributing;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class CollectionAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val collectionIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Collection.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                        .addValidationFailure(processMethodContext.getFacetHolder(), Collection.class));

        inferIntentWhenOnTypeLevel(processMethodContext, collectionIfAny);

        processModify(processMethodContext, collectionIfAny);
        processHidden(processMethodContext, collectionIfAny);
        processTypeOf(processMethodContext, collectionIfAny);
    }

    void inferIntentWhenOnTypeLevel(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {
        if(!processMethodContext.isMixinMain() || !collectionIfAny.isPresent()) {
            return; // no @Collection found neither type nor method
        }

        //          XXX[1998] this condition would allow 'intent inference' only when @Property is found at type level
        //          val isPropertyMethodLevel = processMethodContext.synthesizeOnMethod(Property.class).isPresent();
        //          if(isPropertyMethodLevel) return;

        //[1998] if @Collection detected on method or type level infer:
        //@Action(semantics=SAFE)
        //@ActionLayout(contributed=ASSOCIATION) ... it seems, is already allowed for mixins
        val facetedMethod = processMethodContext.getFacetHolder();
        addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        addFacet(new ContributingFacetAbstract(Contributing.AS_ASSOCIATION, facetedMethod) {});

    }

    void processModify(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {

        val cls = processMethodContext.getCls();
        val typeSpec = getSpecificationLoader().loadSpecification(cls);
        val holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        // following only runs for regular collections, not for mixins.
        // those are tackled in the post-processing, when more of the metamodel is available to us

        //
        // Set up CollectionDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //

        // search for @Collection(domainEvent=...)
        val collectionDomainEventFacet = collectionIfAny
        .map(Collection::domainEvent)
        .filter(domainEvent -> domainEvent != CollectionDomainEvent.Default.class)
        .map(domainEvent ->
                (CollectionDomainEventFacetAbstract)
                new CollectionDomainEventFacetForCollectionAnnotation(
                        defaultFromDomainObjectIfRequired(typeSpec, domainEvent), holder))
        .orElse(
                new CollectionDomainEventFacetDefault(
                        defaultFromDomainObjectIfRequired(typeSpec, CollectionDomainEvent.Default.class), holder));
        if(!CollectionDomainEvent.Noop.class.isAssignableFrom(collectionDomainEventFacet.getEventType())) {
            addFacet(collectionDomainEventFacet);
        }

        if(EventUtil.eventTypeIsPostable(
                collectionDomainEventFacet.getEventType(),
                CollectionDomainEvent.Noop.class,
                CollectionDomainEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getCollection().getDomainEvent().isPostForDefault()
                )) {
            addFacet(collectionDomainEventFacet);
        }

    }

    public static Class<? extends CollectionDomainEvent<?,?>> defaultFromDomainObjectIfRequired(
            final ObjectSpecification typeSpec,
            final Class<? extends CollectionDomainEvent<?,?>> collectionDomainEventType) {
        if (collectionDomainEventType == CollectionDomainEvent.Default.class) {
            final CollectionDomainEventDefaultFacetForDomainObjectAnnotation typeFromDomainObject =
                    typeSpec.getFacet(CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class);
            if (typeFromDomainObject != null) {
                return typeFromDomainObject.getEventType();
            }
        }
        return collectionDomainEventType;
    }


    void processHidden(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Collection(hidden=...)
        addFacetIfPresent(
                HiddenFacetForCollectionAnnotation
                .create(collectionIfAny, holder));
    }


    void processTypeOf(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {

        val facetHolder = processMethodContext.getFacetHolder();
        val method = processMethodContext.getMethod();

        val methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayType(methodReturnType)) {
            return;
        }

        addFacetIfPresent(_Optionals.orNullable(

            // check for @Collection(typeOf=...)
            TypeOfFacetForCollectionAnnotation
            .create(collectionIfAny, facetHolder)

            ,

            // else infer from return type
            ()-> method.getReturnType().isArray()
                        ? TypeOfFacet.inferredFromArray(
                                method.getReturnType().getComponentType(),
                                facetHolder)
                        : null
            ,

            // else infer from generic return type
            ()->inferFromGenericReturnType(processMethodContext)
                    .orElse(null)

        ));

    }

    private Optional<TypeOfFacet> inferFromGenericReturnType(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val method = processMethodContext.getMethod();

        return _Generics.streamGenericTypeArgumentsOfMethodReturnType(method)
                .findFirst()
                .map(elementType->TypeOfFacet.inferredFromGenerics(elementType, facetHolder));
    }



}
