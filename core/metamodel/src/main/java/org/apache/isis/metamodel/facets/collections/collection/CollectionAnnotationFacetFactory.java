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

package org.apache.isis.metamodel.facets.collections.collection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.metamodel.facets.collections.collection.disabled.DisabledFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.hidden.HiddenFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromAbstract;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionAddToFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetDefault;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromAbstract;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionRemoveFromFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.collections.collection.notpersisted.NotPersistedFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.collection.typeof.TypeOfFacetOnCollectionFromCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.util.EventUtil;

import lombok.val;

public class CollectionAnnotationFacetFactory extends FacetFactoryAbstract {

    public CollectionAnnotationFacetFactory() {
        super(FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        
        val collectionIfAny = processMethodContext.synthesizeOnMethodOrMixinType(Collection.class);
        
        inferIntentWhenOnTypeLevel(processMethodContext, collectionIfAny);
        
        processModify(processMethodContext, collectionIfAny);
        processHidden(processMethodContext, collectionIfAny);
        processEditing(processMethodContext, collectionIfAny);
        processNotPersisted(processMethodContext, collectionIfAny);
        processTypeOf(processMethodContext, collectionIfAny);
    }

    void inferIntentWhenOnTypeLevel(ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {
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
        FacetUtil.addOrReplaceFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        //facetedMethod.addFacet(new NotContributedFacetForActionLayoutAnnotation(Contributed.AS_ASSOCIATION, facetedMethod));

    }

    void processModify(final ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {

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
                                defaultFromDomainObjectIfRequired(typeSpec, CollectionDomainEvent.Default.class), holder)
                        );
        if(!CollectionDomainEvent.Noop.class.isAssignableFrom(collectionDomainEventFacet.getEventType())) {
            super.addFacet(collectionDomainEventFacet);
        }

        if(EventUtil.eventTypeIsPostable(
                collectionDomainEventFacet.getEventType(),
                CollectionDomainEvent.Noop.class,
                CollectionDomainEvent.Default.class,
                getConfiguration().getReflector().getFacet().getCollectionAnnotation().getDomainEvent().isPostForDefault()
                )) {
            super.addFacet(collectionDomainEventFacet);
        }


        //
        // if the collection is mutable, then replace the existing addTo and removeFrom facets with equivalents that
        // also post to the event bus.
        //
        // here we support the deprecated annotations
        //
        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        if (collectionAddToFacet != null) {
            // the current collectionAddToFacet will end up as the underlying facet of
            // one of these facets to be created.
            final CollectionAddToFacetForDomainEventFromAbstract replacementFacet;

            if(collectionDomainEventFacet instanceof CollectionDomainEventFacetForCollectionAnnotation) {
                replacementFacet = new CollectionAddToFacetForDomainEventFromCollectionAnnotation(
                        collectionDomainEventFacet.getEventType(), getterFacet, collectionAddToFacet, 
                        collectionDomainEventFacet, holder, getServiceRegistry());
            } else
                // default
            {
                replacementFacet = new CollectionAddToFacetForDomainEventFromDefault(
                        collectionDomainEventFacet.getEventType(), getterFacet, 
                        collectionAddToFacet, collectionDomainEventFacet, holder, getServiceRegistry());
            }
            super.addFacet(replacementFacet);
        }

        final CollectionRemoveFromFacet collectionRemoveFromFacet = holder.getFacet(CollectionRemoveFromFacet.class);
        if (collectionRemoveFromFacet != null) {
            // the current collectionRemoveFromFacet will end up as the underlying facet of the PostsCollectionRemovedFromEventFacetAnnotation

            final CollectionRemoveFromFacetForDomainEventFromAbstract replacementFacet;

            if(collectionDomainEventFacet instanceof CollectionDomainEventFacetForCollectionAnnotation) {
                replacementFacet = new CollectionRemoveFromFacetForDomainEventFromCollectionAnnotation(collectionDomainEventFacet.getEventType(), getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, getServiceRegistry(), holder);
            } else {
                // default
                replacementFacet = new CollectionRemoveFromFacetForDomainEventFromDefault(collectionDomainEventFacet.getEventType(), getterFacet, collectionRemoveFromFacet, collectionDomainEventFacet, getServiceRegistry(), holder);
            }
            super.addFacet(replacementFacet);
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


    void processHidden(final ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Collection(hidden=...)
        val facet = HiddenFacetForCollectionAnnotation.create(collectionIfAny, holder);

        super.addFacet(facet);
    }

    void processEditing(final ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Collection(editing=...)
        val facet = DisabledFacetForCollectionAnnotation.create(collectionIfAny, holder);

        super.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Collection(notPersisted=...)
        val facet = NotPersistedFacetForCollectionAnnotation.create(collectionIfAny, holder);

        super.addFacet(facet);
    }


    void processTypeOf(final ProcessMethodContext processMethodContext, Optional<Collection> collectionIfAny) {

        val facetHolder = processMethodContext.getFacetHolder();
        val method = processMethodContext.getMethod();

        val methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayType(methodReturnType)) {
            return;
        }

        // check for @Collection(typeOf=...)
        TypeOfFacet facet = TypeOfFacetOnCollectionFromCollectionAnnotation
                .create(collectionIfAny, facetHolder);

        // else infer from return type
        if(facet == null) {
            val returnType = method.getReturnType();
            if (returnType.isArray()) {
                val componentType = returnType.getComponentType();
                facet = new TypeOfFacetInferredFromArray(componentType, facetHolder);
            }
        }

        // else infer from generic return type
        if(facet == null) {
            facet = inferFromGenericReturnType(processMethodContext);
        }

        super.addFacet(facet);
    }

    private TypeOfFacet inferFromGenericReturnType(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val method = processMethodContext.getMethod();

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
            val actualType = (Class<?>) actualTypeArgument;
            return new TypeOfFacetInferredFromGenerics(actualType, facetHolder);
        }

        if (actualTypeArgument instanceof TypeVariable) {

            // TODO: what to do here?
            return null;
        }

        return null;
    }


}
