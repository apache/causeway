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

package org.apache.isis.metamodel.facets.properties.property;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.facets.properties.projection.ProjectingFacetFromPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.command.CommandFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.fileaccept.FileAcceptFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByNullableAnnotationOnProperty;
import org.apache.isis.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.publishing.PublishedPropertyFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;
import org.apache.isis.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.metamodel.util.EventUtil;

import lombok.val;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract 
implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForConflictingOptionality conflictingOptionalityValidator = 
            new MetaModelValidatorForConflictingOptionality();


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

        val cls = processMethodContext.getCls();
        val typeSpec = getSpecificationLoader().loadSpecification(cls);
        val holder = processMethodContext.getFacetHolder();

        val getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return;
        }

        // following only runs for regular properties, not for mixins.
        // those are tackled in the post-processing, when more of the metamodel is available to us


        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);

        // search for @Property(domainEvent=...), else use default event type
        val propertyDomainEventFacet = propertyIfAny
                .map(Property::domainEvent)
                .filter(domainEvent -> domainEvent != PropertyDomainEvent.Default.class)
                .map(domainEvent -> (PropertyDomainEventFacetAbstract) new PropertyDomainEventFacetForPropertyAnnotation(
                        defaultFromDomainObjectIfRequired(typeSpec, domainEvent), getterFacet, holder))
                .orElse(new PropertyDomainEventFacetDefault(
                        defaultFromDomainObjectIfRequired(typeSpec, PropertyDomainEvent.Default.class), getterFacet,
                        holder));

        if(EventUtil.eventTypeIsPostable(
                propertyDomainEventFacet.getEventType(),
                PropertyDomainEvent.Noop.class,
                PropertyDomainEvent.Default.class,
                getConfiguration().getReflector().getFacet().getPropertyAnnotation().getDomainEvent().isPostForDefault()
                )) {
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
                        propertyDomainEventFacet.getEventType(), getterFacet, setterFacet, propertyDomainEventFacet, holder);
            } else
                // default
            {
                replacementFacet = new PropertySetterFacetForDomainEventFromDefault(
                        propertyDomainEventFacet.getEventType(), getterFacet, setterFacet, propertyDomainEventFacet, holder);
            }
            FacetUtil.addFacet(replacementFacet);
        }

        final PropertyClearFacet clearFacet = holder.getFacet(PropertyClearFacet.class);
        if(clearFacet != null) {
            // the current clear facet will end up as the underlying facet
            final PropertyClearFacet replacementFacet;

            if(propertyDomainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation) {
                replacementFacet = new PropertyClearFacetForDomainEventFromPropertyAnnotation(
                        propertyDomainEventFacet.getEventType(), getterFacet, clearFacet, propertyDomainEventFacet, holder);
            } else
                // default
            {
                replacementFacet = new PropertyClearFacetForDomainEventFromDefault(
                        propertyDomainEventFacet.getEventType(), getterFacet, clearFacet, propertyDomainEventFacet, holder);
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
        val facetHolder = processMethodContext.getFacetHolder();
        
        // search for @Property(hidden=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val hiddenFacet = HiddenFacetForPropertyAnnotation.create(propertyIfAny, facetHolder);

        FacetUtil.addFacet(hiddenFacet);
    }

    void processEditing(final ProcessMethodContext processMethodContext) {
        val facetHolder = processMethodContext.getFacetHolder();

        // search for @Property(editing=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val disabledFacet = DisabledFacetForPropertyAnnotation.create(propertyIfAny, facetHolder);

        FacetUtil.addFacet(disabledFacet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {
        val facetHolder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(command=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val commandFacet = CommandFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), facetHolder, getServiceInjector());

        FacetUtil.addFacet(commandFacet);
    }

    void processProjecting(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);

        val projectingFacet = ProjectingFacetFromPropertyAnnotation
                .create(propertyIfAny, facetHolder);

        FacetUtil.addFacet(projectingFacet);

    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        
        val holder = processMethodContext.getFacetHolder();

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
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet = PublishedPropertyFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), holder);

        FacetUtil.addFacet(facet);
    }



    void processMaxLength(final ProcessMethodContext processMethodContext) {

        val holder = processMethodContext.getFacetHolder();

        // search for @Property(maxLength=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet = MaxLengthFacetForPropertyAnnotation.create(propertyIfAny, holder);

        FacetUtil.addFacet(facet);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(mustSatisfy=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(propertyIfAny, holder, getServiceInjector());

        FacetUtil.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(notPersisted=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet = NotPersistedFacetForPropertyAnnotation.create(propertyIfAny, holder);

        FacetUtil.addFacet(facet);
    }

    void processOptional(final ProcessMethodContext processMethodContext) {

        val method = processMethodContext.getMethod();

        val holder = processMethodContext.getFacetHolder();

        // check for @Nullable
        val nullableIfAny = processMethodContext.synthesizeOnMethod(Nullable.class);
        val facet2 =
                MandatoryFacetInvertedByNullableAnnotationOnProperty.create(nullableIfAny, method, holder);
        FacetUtil.addFacet(facet2);
        conflictingOptionalityValidator.flagIfConflict(
                facet2, "Conflicting @Nullable with other optionality annotation");

        // search for @Property(optional=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet3 = MandatoryFacetForPropertyAnnotation.create(propertyIfAny, method, holder);
        FacetUtil.addFacet(facet3);
        conflictingOptionalityValidator.flagIfConflict(
                facet3, "Conflicting Property#optionality with other optionality annotation");
    }

    void processRegEx(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();
        val returnType = processMethodContext.getMethod().getReturnType();

        // check for @Pattern first
        val patternIfAny = processMethodContext.synthesizeOnMethod(Pattern.class);
        val facet = RegExFacetForPatternAnnotationOnProperty.create(patternIfAny, returnType, holder);

        if (facet != null) {
            FacetUtil.addFacet(facet);
            return;
        }
        
        // else search for @Property(pattern=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet2 = RegExFacetForPropertyAnnotation.create(propertyIfAny, returnType, holder);
        FacetUtil.addFacet(facet2);
        
        
    }


    void processFileAccept(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // else search for @Property(maxLength=...)
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        val facet = FileAcceptFacetForPropertyAnnotation.create(propertyIfAny, holder);

        FacetUtil.addFacet(facet);
    }

    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(conflictingOptionalityValidator);
    }



}
