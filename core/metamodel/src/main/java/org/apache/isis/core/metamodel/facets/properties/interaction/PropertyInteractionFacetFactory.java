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

package org.apache.isis.core.metamodel.facets.properties.interaction;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyInteraction;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyInteractionFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class PropertyInteractionFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, MetaModelValidatorRefiner {

    private ServicesInjector servicesInjector;

    public PropertyInteractionFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        FacetedMethod holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        //
        // Set up PropertyInteractionFacet, which will act as the hiding/disabling/validating advisor
        //
        final Property property = Annotations.getAnnotation(method, Property.class);
        final PropertyInteraction propertyInteraction = Annotations.getAnnotation(method, PropertyInteraction.class);
        final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType;

        final PropertyInteractionFacetAbstract propertyInteractionFacet;
        if(property != null && property.domainEvent() != null) {
            propertyDomainEventType = property.domainEvent();
            propertyInteractionFacet = new PropertyInteractionFacetForPropertyAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);

        } else if(propertyInteraction != null) {
            propertyDomainEventType = propertyInteraction.value();
            propertyInteractionFacet = new PropertyInteractionFacetAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);
        } else {
            propertyDomainEventType = PropertyDomainEvent.Default.class;
            propertyInteractionFacet = new PropertyInteractionFacetDefault(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);
        }
        FacetUtil.addFacet(propertyInteractionFacet);


        //
        // if the property is mutable, then replace the existing setter and clear facets with equivalents that
        // also post to the event bus.
        //
        // here we support the deprecated annotations
        //


        final PostsPropertyChangedEvent postsPropertyChangedEvent = Annotations.getAnnotation(method, PostsPropertyChangedEvent.class);

        final PropertySetterFacet setterFacet = holder.getFacet(PropertySetterFacet.class);
        if(setterFacet != null) {
            // the current setter facet will end up as the underlying facet
            final PropertySetterFacetForInteractionAbstract replacementFacet;
            if(propertyInteraction != null) {
                replacementFacet = new PropertySetterFacetForPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, setterFacet, propertyInteractionFacet, holder, servicesInjector);
            } else if(postsPropertyChangedEvent != null) {
                replacementFacet = new PropertySetterFacetForPostsPropertyChangedEventAnnotation(
                        postsPropertyChangedEvent.value(), getterFacet, setterFacet, propertyInteractionFacet, holder, servicesInjector);
            } else {
                replacementFacet = new PropertySetterFacetForPropertyInteractionDefault(
                        PropertyDomainEvent.Default.class, getterFacet, setterFacet, propertyInteractionFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }
        
        final PropertyClearFacet clearFacet = holder.getFacet(PropertyClearFacet.class);
        if(clearFacet != null) {
            // the current clear facet will end up as the underlying facet
            final PropertyClearFacetForInteractionAbstract replacementFacet;
            if(propertyInteraction != null) {
                replacementFacet = new PropertyClearFacetForPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, clearFacet, propertyInteractionFacet, holder, servicesInjector);
            } else if(postsPropertyChangedEvent != null) {
                replacementFacet = new PropertyClearFacetForPostsPropertyChangedEventAnnotation(
                        postsPropertyChangedEvent.value(), getterFacet, clearFacet, propertyInteractionFacet, holder, servicesInjector);
            } else {
                replacementFacet = new PropertyClearFacetForPropertyInteractionDefault(
                        PropertyDomainEvent.Default.class, getterFacet, clearFacet, propertyInteractionFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }
    }

    // //////////////////////////////////////
    
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    protected Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                List<OneToManyAssociation> objectCollections = objectSpec.getCollections(Contributed.EXCLUDED);
                for (OneToManyAssociation objectCollection : objectCollections) {
                    final SortedByFacet facet = objectCollection.getFacet(SortedByFacet.class);
                    if(facet != null) {
                        final Class<? extends Comparator<?>> cls = facet.value();
                        if(!Comparator.class.isAssignableFrom(cls)) {
                            validationFailures.add("%s#%s is annotated with @SortedBy, but the class specified '%s' is not a Comparator", objectSpec.getIdentifier().getClassName(), objectCollection.getId(), facet.value().getName());
                        }
                    }
                }
                return true;
            }
        };
    }

    // //////////////////////////////////////

    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
