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

package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyInteraction;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, ContributeeMemberFacetFactory {

    private ServicesInjector servicesInjector;

    public PropertyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processModify(processMethodContext);
        processHidden(processMethodContext);
        processEditing(processMethodContext);
        processMaxLength(processMethodContext);
        processMustSatisfy(processMethodContext);
        processNotPersisted(processMethodContext);
        processOptional(processMethodContext);
        processRegEx(processMethodContext);
    }

    void processModify(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //
        final PostsPropertyChangedEvent postsPropertyChangedEvent = Annotations.getAnnotation(method, PostsPropertyChangedEvent.class);
        final Property property = Annotations.getAnnotation(method, Property.class);
        final PropertyInteraction propertyInteraction = Annotations.getAnnotation(method, PropertyInteraction.class);
        final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType;

        final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

        // search for @PropertyInteraction(value=...)
        if(propertyInteraction != null) {
            propertyDomainEventType = propertyInteraction.value();
            propertyDomainEventFacet = new PropertyDomainEventFacetForPropertyInteractionAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);
        } else
        // search for @Property(domainEvent=...)
        if(property != null && property.domainEvent() != null) {
            propertyDomainEventType = property.domainEvent();
            propertyDomainEventFacet = new PropertyDomainEventFacetForPropertyAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);

        } else
        // else use default event type (also for @PostsPropertyChangedEvent)
        {
            propertyDomainEventType = PropertyDomainEvent.Default.class;
            propertyDomainEventFacet = new PropertyDomainEventFacetDefault(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);
        }
        FacetUtil.addFacet(propertyDomainEventFacet);


        //
        // if the property is mutable, then replace the current setter and clear facets with equivalents that
        // emit the appropriate domain event and then delegate onto the underlying
        //

        final PropertySetterFacet setterFacet = holder.getFacet(PropertySetterFacet.class);
        if(setterFacet != null) {
            // the current setter facet will end up as the underlying facet
            final PropertySetterFacetForDomainEventAbstract replacementFacet;
            // deprecated
            if(postsPropertyChangedEvent != null) {
                final Class<? extends PropertyChangedEvent<?, ?>> propertySetEventType = postsPropertyChangedEvent.value();
                replacementFacet = new PropertySetterFacetForPostsPropertyChangedEventAnnotation(
                        propertySetEventType, getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            // deprecated (but more recently)
            if(propertyInteraction != null) {
                replacementFacet = new PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            // current
            if(property != null) {
                replacementFacet = new PropertySetterFacetForDomainEventFromPropertyAnnotation(
                        propertyDomainEventType, getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            // default
            {
                replacementFacet = new PropertySetterFacetForDomainEventFromDefault(
                        propertyDomainEventType, getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }

        final PropertyClearFacet clearFacet = holder.getFacet(PropertyClearFacet.class);
        if(clearFacet != null) {
            // the current clear facet will end up as the underlying facet
            final PropertyClearFacetForDomainEventAbstract replacementFacet;

            // deprecated
            if(postsPropertyChangedEvent != null) {
                final Class<? extends PropertyChangedEvent<?, ?>> propertyClearEventType = postsPropertyChangedEvent.value();
                replacementFacet = new PropertyClearFacetForPostsPropertyChangedEventAnnotation(
                        propertyClearEventType, getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            // deprecated (but more recently)
            if(propertyInteraction != null) {
                replacementFacet = new PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            // current
            if(property != null) {
                replacementFacet = new PropertyClearFacetForDomainEventFromPropertyAnnotation(
                        propertyDomainEventType, getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
            {
                replacementFacet = new PropertyClearFacetForDomainEventFromDefault(
                        propertyDomainEventType, getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }
    }

    void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final HiddenFacet facet = HiddenFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final DisabledFacet facet = DisabledFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }

    void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final MaxLengthFacet facet = MaxLengthFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final Facet facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final NotPersistedFacet facet = NotPersistedFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }

    void processOptional(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final MandatoryFacet facet = MandatoryFacetForPropertyAnnotation.create(property, method, holder);

        FacetUtil.addFacet(facet);
    }

    void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        if (!Annotations.isString(returnType)) {
            return;
        }

        final RegExFacet facet = RegExFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }


    @Override
    public void process(ContributeeMemberFacetFactory.ProcessContributeeMemberContext processMemberContext) {

        final ObjectMember objectMember = processMemberContext.getFacetHolder();

        //
        // an enhancement would be to pick up a custom event, however the contributed property ultimately maps
        // to an action on a service, and would therefore require a @Property(...) annotated on an action;
        // would look rather odd
        //

        final PropertyOrCollectionAccessorFacet accessorFacet =
                objectMember.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(accessorFacet != null) {
            final PropertyDomainEventFacet facet = new PropertyDomainEventFacetDefault(
                    PropertyDomainEvent.Default.class, accessorFacet, servicesInjector, getSpecificationLoader(), objectMember);

            FacetUtil.addFacet(facet);
        }

    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
