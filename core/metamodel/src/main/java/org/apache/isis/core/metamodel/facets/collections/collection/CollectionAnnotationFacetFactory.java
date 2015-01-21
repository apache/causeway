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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionInteraction;
import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.annotation.PostsCollectionRemovedFromEvent;
import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.collections.collection.disabled.DisabledFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionAddToFacetForPostsCollectionAddedToEventAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromTypeOfAnnotation;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;

public class CollectionAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, ContributeeMemberFacetFactory {

    private final CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistry();

    private ServicesInjector servicesInjector;

    public CollectionAnnotationFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processModify(processMethodContext);
        processHidden(processMethodContext);
        processEditing(processMethodContext);
        processTypeOf(processMethodContext);
    }

    void processModify(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        //
        // Set up CollectionDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final CollectionInteraction collectionInteraction = Annotations.getAnnotation(method, CollectionInteraction.class);
        final Class<? extends CollectionDomainEvent<?, ?>> collectionDomainEventType;

        final CollectionDomainEventFacetAbstract collectionDomainEventFacet;

        // search for @PropertyInteraction(value=...)
        if(collectionInteraction != null) {
            collectionDomainEventType = collectionInteraction.value();
            collectionDomainEventFacet = new CollectionDomainEventFacetForCollectionInteractionAnnotation(
                    collectionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
        } else
        // search for @Property(domainEvent=...)
        if(collection != null && collection.domainEvent() != null) {
            collectionDomainEventType = collection.domainEvent();
            collectionDomainEventFacet = new CollectionDomainEventFacetForCollectionAnnotation(
                    collectionDomainEventType, servicesInjector, getSpecificationLoader(), holder);

        } else
        // else use default event type
        {
            collectionDomainEventType = CollectionDomainEvent.Default.class;
            collectionDomainEventFacet = new CollectionDomainEventFacetDefault(
                    collectionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
        }
        FacetUtil.addFacet(collectionDomainEventFacet);


        //
        // if the collection is mutable, then replace the existing addTo and removeFrom facets with equivalents that
        // also post to the event bus.
        //
        // here we support the deprecated annotations
        //
        final PostsCollectionAddedToEvent postsCollectionAddedToEvent = Annotations.getAnnotation(method, PostsCollectionAddedToEvent.class);
        final PostsCollectionRemovedFromEvent postsCollectionRemovedFromEvent = Annotations.getAnnotation(method, PostsCollectionRemovedFromEvent.class);

        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        if (collectionAddToFacet != null) {
            // the current collectionAddToFacet will end up as the underlying facet of
            // one of these facets to be created.
            final CollectionAddToFacetForDomainEventFromAbstract replacementFacet;

            // deprecated
            if (postsCollectionAddedToEvent != null) {
                replacementFacet = new CollectionAddToFacetForPostsCollectionAddedToEventAnnotation(
                        postsCollectionAddedToEvent.value(), getterFacet, collectionAddToFacet, collectionDomainEventFacet, holder, servicesInjector);
            } else
            // deprecated (but more recently)
            if(collectionInteraction != null) {
                replacementFacet = new CollectionAddToFacetForDomainEventFromCollectionInteractionAnnotation(
                        collectionDomainEventType, getterFacet, collectionAddToFacet, collectionDomainEventFacet, holder, servicesInjector);
            } else
            // current
            if(collection != null) {
                replacementFacet = new CollectionAddToFacetForDomainEventFromCollectionAnnotation(
                        collectionDomainEventType, getterFacet, collectionAddToFacet, collectionDomainEventFacet, holder, servicesInjector);
            } else
            // default
            {
                replacementFacet = new CollectionAddToFacetForDomainEventFromDefault(
                        collectionDomainEventType, getterFacet, collectionAddToFacet, collectionDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }

        final CollectionRemoveFromFacet collectionRemoveFromFacet = holder.getFacet(CollectionRemoveFromFacet.class);
        if (collectionRemoveFromFacet != null) {
            // the current collectionRemoveFromFacet will end up as the underlying facet of the PostsCollectionRemovedFromEventFacetAnnotation

            final CollectionRemoveFromFacetForDomainEventFromAbstract replacementFacet;

            // deprecated
            if (postsCollectionRemovedFromEvent != null) {
                replacementFacet = new CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation(postsCollectionRemovedFromEvent.value(), getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, servicesInjector, holder);
            } else
            // deprecated (but more recently)
            if(collectionInteraction != null) {
                replacementFacet = new CollectionRemoveFromFacetForDomainEventFromCollectionInteractionAnnotation(collectionDomainEventType, getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, servicesInjector, holder);
            } else
            // current
            if(collection != null) {
                replacementFacet = new CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation(collectionDomainEventType, getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, servicesInjector, holder);
            } else
            // default
            {
                replacementFacet = new CollectionRemoveFromFacetForDomainEventFromDefault(collectionDomainEventType, getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, servicesInjector, holder);
            }
            FacetUtil.addFacet(replacementFacet);
        }

    }

    void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                HiddenFacetForCollectionAnnotation.create(collection, holder));
    }

    void processEditing(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                DisabledFacetForCollectionAnnotation.create(collection, holder));
    }

    void processTypeOf(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        final Method method = processMethodContext.getMethod();

        final Class<?> methodReturnType = method.getReturnType();
        if (!collectionTypeRegistry.isCollectionType(methodReturnType) && !collectionTypeRegistry.isArrayType(methodReturnType)) {
            return;
        }

        TypeOfFacet facet;

        // check for deprecated @TypeOf
        final TypeOf annotation = Annotations.getAnnotation(method, TypeOf.class);
        facet = TypeOfFacetOnCollectionFromTypeOfAnnotation.create(annotation, facetHolder, getSpecificationLoader());

        // check for @Collection(typeOf=...)
        if(facet == null) {
            final Collection collection = Annotations.getAnnotation(method, Collection.class);
            facet = TypeOfFacetOnCollectionFromCollectionAnnotation.create(collection, facetHolder, getSpecificationLoader());
        }

        // infer from return type
        if(facet == null) {
            final Class<?> returnType = method.getReturnType();
            if (returnType.isArray()) {
                final Class<?> componentType = returnType.getComponentType();
                facet = new TypeOfFacetInferredFromArray(componentType, facetHolder, getSpecificationLoader());
            }
        }

        // infer from generic return type
        if(facet == null) {
            facet = inferFromGenericReturnType(processMethodContext);
        }


        FacetUtil.addFacet(facet);
    }

    private TypeOfFacet inferFromGenericReturnType(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        final Method method = processMethodContext.getMethod();

        final Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        final ParameterizedType parameterizedType = (ParameterizedType) type;
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return null;
        }

        final Object actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) actualTypeArgument;
            return new TypeOfFacetInferredFromGenerics(actualType, facetHolder, getSpecificationLoader());
        }

        if (actualTypeArgument instanceof TypeVariable) {

            // TODO: what to do here?
            return null;
        }

        return null;
    }

    @Override
    public void process(ContributeeMemberFacetFactory.ProcessContributeeMemberContext processMemberContext) {

        final ObjectMember objectMember = processMemberContext.getFacetHolder();

        //
        // an enhancement would be to pick up a custom event, however the contributed collection ultimately maps
        // to an action on a service, and would therefore require a @Collection(...) annotated on an action;
        // would look rather odd
        //

        FacetUtil.addFacet(new CollectionDomainEventFacetDefault(CollectionDomainEvent.Default.class, servicesInjector, getSpecificationLoader(), objectMember));

    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
