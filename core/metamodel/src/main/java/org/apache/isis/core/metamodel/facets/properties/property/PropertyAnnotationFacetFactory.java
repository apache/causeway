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
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.projection.ProjectingFacet;
import org.apache.isis.core.metamodel.facets.properties.projection.ProjectingFacetFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.command.CommandFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.fileaccept.FileAcceptFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByNullableAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.publishing.PublishedPropertyFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.core.metamodel.util.EventUtil;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForConflictingOptionality conflictingOptionalityValidator = new MetaModelValidatorForConflictingOptionality();


    public PropertyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        processModify(processMethodContext);
        processHidden(processMethodContext);
        processEditing(processMethodContext);
        processCommand(processMethodContext);
        processProjecting(processMethodContext);
        processPublishing(processMethodContext);
        processMaxLength(processMethodContext);
        processMustSatisfy(processMethodContext);
        processNotPersisted(processMethodContext);
        processOptional(processMethodContext);
        processRegEx(processMethodContext);
        processFileAccept(processMethodContext);
    }


    void processModify(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();

        final Class<?> cls = processMethodContext.getCls();
        final ObjectSpecification typeSpec = getSpecificationLoader().loadSpecification(cls);
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        // following only runs for regular properties, not for mixins.
        // those are tackled in the post-processing, when more of the metamodel is available to us


        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);

        // search for @Property(domainEvent=...), else use default event type
        final PropertyDomainEventFacetAbstract propertyDomainEventFacet = properties.stream()
                .map(Property::domainEvent)
                .filter(domainEvent -> domainEvent != PropertyDomainEvent.Default.class)
                .findFirst()
                .map(domainEvent -> (PropertyDomainEventFacetAbstract) new PropertyDomainEventFacetForPropertyAnnotation(
                        defaultFromDomainObjectIfRequired(typeSpec, domainEvent), getterFacet, servicesInjector, getSpecificationLoader(), holder))
                .orElse(new PropertyDomainEventFacetDefault(
                        defaultFromDomainObjectIfRequired(typeSpec, PropertyDomainEvent.Default.class), getterFacet, servicesInjector, getSpecificationLoader(),
                        holder));

        if(EventUtil.eventTypeIsPostable(
                propertyDomainEventFacet.getEventType(),
                PropertyDomainEvent.Noop.class,
                PropertyDomainEvent.Default.class,
                "isis.reflector.facet.propertyAnnotation.domainEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(propertyDomainEventFacet);
        }


        //
        // if the property is mutable, then replace the current setter and clear facets with equivalents that
        // emit the appropriate domain event and then delegate onto the underlying
        //

        final PropertySetterFacet setterFacet = holder.getFacet(PropertySetterFacet.class);
        if(setterFacet != null) {
            // the current setter facet will end up as the underlying facet
            final PropertySetterFacet replacementFacet;

            if(propertyDomainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation) {
                replacementFacet = new PropertySetterFacetForDomainEventFromPropertyAnnotation(
                        propertyDomainEventFacet.getEventType(), getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
                // default
            {
                replacementFacet = new PropertySetterFacetForDomainEventFromDefault(
                        propertyDomainEventFacet.getEventType(), getterFacet, setterFacet, propertyDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }

        final PropertyClearFacet clearFacet = holder.getFacet(PropertyClearFacet.class);
        if(clearFacet != null) {
            // the current clear facet will end up as the underlying facet
            final PropertyClearFacet replacementFacet;

            if(propertyDomainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation) {
                replacementFacet = new PropertyClearFacetForDomainEventFromPropertyAnnotation(
                        propertyDomainEventFacet.getEventType(), getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            } else
                // default
            {
                replacementFacet = new PropertyClearFacetForDomainEventFromDefault(
                        propertyDomainEventFacet.getEventType(), getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }
    }

    public static Class<? extends PropertyDomainEvent<?,?>> defaultFromDomainObjectIfRequired(
            final ObjectSpecification typeSpec,
            final Class<? extends PropertyDomainEvent<?,?>> propertyDomainEventType) {
        if (propertyDomainEventType == PropertyDomainEvent.Default.class) {
            final PropertyDomainEventDefaultFacetForDomainObjectAnnotation typeFromDomainObject =
                    typeSpec.getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
            if (typeFromDomainObject != null) {
                return typeFromDomainObject.getEventType();
            }
        }
        return propertyDomainEventType;
    }



    void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Property(hidden=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        HiddenFacet facet = HiddenFacetForPropertyAnnotation.create(properties, holder);

        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Property(editing=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        DisabledFacet facet = DisabledFacetForPropertyAnnotation.create(properties, holder);

        FacetUtil.addFacet(facet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final FacetHolder holder = facetHolder;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(command=...)
        final CommandFacet commandFacet = CommandFacetForPropertyAnnotation.create(properties, getConfiguration(), holder, servicesInjector);

        FacetUtil.addFacet(commandFacet);
    }

    void processProjecting(final ProcessMethodContext processMethodContext) {

        final Class<?> cls = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final ProjectingFacet projectingFacet = ProjectingFacetFromPropertyAnnotation
                .create(property, facetHolder);

        FacetUtil.addFacet(projectingFacet);

    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(publishing=...)
        final PublishedPropertyFacet facet = PublishedPropertyFacetForPropertyAnnotation
                .create(properties, getConfiguration(), holder);

        FacetUtil.addFacet(facet);
    }



    void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Property(maxLength=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        MaxLengthFacet facet = MaxLengthFacetForPropertyAnnotation.create(properties, holder);

        FacetUtil.addFacet(facet);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Property(mustSatisfy=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        Facet facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(properties, holder, servicesInjector);

        FacetUtil.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Property(notPersisted=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        NotPersistedFacet facet = NotPersistedFacetForPropertyAnnotation.create(properties, holder);

        FacetUtil.addFacet(facet);
    }

    void processOptional(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();

        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for @Nullable
        final List<Nullable> nullableAnnotations = Annotations.getAnnotations(method, Nullable.class);
        final Nullable nullableAnnotation = nullableAnnotations.isEmpty() ? null : nullableAnnotations.get(0);
        final MandatoryFacet facet2 =
                MandatoryFacetInvertedByNullableAnnotationOnProperty.create(nullableAnnotation, method, holder);
        FacetUtil.addFacet(facet2);
        conflictingOptionalityValidator.flagIfConflict(
                facet2, "Conflicting @Nullable with other optionality annotation");

        // search for @Property(optional=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        final MandatoryFacet facet3 = MandatoryFacetForPropertyAnnotation.create(properties, method, holder);
        FacetUtil.addFacet(facet3);
        conflictingOptionalityValidator.flagIfConflict(
                facet3, "Conflicting Property#optionality with other optionality annotation");
    }

    void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();

        // check for @Pattern first
        final List<Pattern> patterns = Annotations.getAnnotations(processMethodContext.getMethod(), Pattern.class);
        RegExFacet facet = RegExFacetForPatternAnnotationOnProperty.create(patterns, returnType, holder);

        // else search for @Property(pattern=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        if (facet == null) {
            facet = RegExFacetForPropertyAnnotation.create(properties, returnType, holder);
        }

        FacetUtil.addFacet(facet);
    }


    void processFileAccept(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();


        // else search for @Property(maxLength=...)
        final List<Property> properties = Annotations.getAnnotations(method, Property.class);
        FileAcceptFacet facet = FileAcceptFacetForPropertyAnnotation.create(properties, holder);

        FacetUtil.addFacet(facet);
    }


    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(conflictingOptionalityValidator);
    }



}
