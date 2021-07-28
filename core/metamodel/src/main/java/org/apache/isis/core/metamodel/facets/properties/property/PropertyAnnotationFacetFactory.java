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

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacet.Contributing;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.publish.command.CommandPublishingFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingPropertyFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.projection.ProjectingFacetFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacetForPropertyAnnotation;
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
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.snapshot.SnapshotExcludeFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class PropertyAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public PropertyAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val propertyIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Property.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                            .addValidationFailure(processMethodContext.getFacetHolder(), Property.class));

        inferIntentWhenOnTypeLevel(processMethodContext, propertyIfAny);

        processModify(processMethodContext, propertyIfAny);
        processHidden(processMethodContext, propertyIfAny);
        processEditing(processMethodContext, propertyIfAny);
        processCommandPublishing(processMethodContext, propertyIfAny);
        processProjecting(processMethodContext, propertyIfAny);
        processExecutionPublishing(processMethodContext, propertyIfAny);
        processMaxLength(processMethodContext, propertyIfAny);
        processMustSatisfy(processMethodContext, propertyIfAny);
        processEntityPropertyChangePublishing(processMethodContext, propertyIfAny);
        processSnapshot(processMethodContext, propertyIfAny);
        processOptional(processMethodContext, propertyIfAny);
        processRegEx(processMethodContext, propertyIfAny);
        processFileAccept(processMethodContext, propertyIfAny);
    }

    void inferIntentWhenOnTypeLevel(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        if(!processMethodContext.isMixinMain() || !propertyIfAny.isPresent()) {
            return; // no @Property found neither type nor method
        }

        //          XXX[1998] this condition would allow 'intent inference' only when @Property is found at type level
        //          val isPropertyMethodLevel = processMethodContext.synthesizeOnMethod(Property.class).isPresent();
        //          if(isPropertyMethodLevel) return;

        //[1998] if @Property detected on method or type level infer:
        //@Action(semantics=SAFE)
        //@ActionLayout(contributed=ASSOCIATION) ... it seems, is already allowed for mixins
        val facetedMethod = processMethodContext.getFacetHolder();
        addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        addFacet(new ContributingFacetAbstract(Contributing.AS_ASSOCIATION, facetedMethod) {});
    }

    void processModify(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

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
                getConfiguration().getApplib().getAnnotation().getProperty().getDomainEvent().isPostForDefault()
                )) {
            addFacet(propertyDomainEventFacet);
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
            addFacet(replacementFacet);
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
            addFacet(replacementFacet);
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

    void processHidden(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();

        // search for @Property(hidden=...)
        addFacetIfPresent(
                HiddenFacetForPropertyAnnotation
                .create(propertyIfAny, facetHolder));
    }

    void processEditing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();

        // search for @Property(editing=...)
        addFacetIfPresent(
                DisabledFacetForPropertyAnnotation
                .create(propertyIfAny, facetHolder));
    }

    void processCommandPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(commandPublishing=...)
        addFacetIfPresent(
                CommandPublishingFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), facetHolder,  getServiceInjector()));
    }

    void processProjecting(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        val facetHolder = processMethodContext.getFacetHolder();

        addFacetIfPresent(
                ProjectingFacetFromPropertyAnnotation
                .create(propertyIfAny, facetHolder));

    }

    void processExecutionPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {

        val holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(executionPublishing=...)
        addFacetIfPresent(
                ExecutionPublishingPropertyFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), holder));
    }



    void processMaxLength(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        val holder = processMethodContext.getFacetHolder();

        // search for @Property(maxLength=...)
        addFacetIfPresent(
                MaxLengthFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(mustSatisfy=...)
        addFacetIfPresent(
                MustSatisfySpecificationFacetForPropertyAnnotation
                .create(propertyIfAny, holder, getFactoryService()));
    }

    void processEntityPropertyChangePublishing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(entityPropertyChangePublishing=...)
        addFacetIfPresent(
                EntityPropertyChangePublishingPolicyFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processSnapshot(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(notPersisted=...)
        addFacetIfPresent(
                SnapshotExcludeFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processOptional(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        val method = processMethodContext.getMethod();

        val holder = processMethodContext.getFacetHolder();

        // check for @Nullable
        val nullableIfAny = processMethodContext.synthesizeOnMethod(Nullable.class);

        addFacetIfPresent(
                MandatoryFacetInvertedByNullableAnnotationOnProperty
                .create(nullableIfAny, method, holder))
        .ifPresent(mandatoryFacet->
                MetaModelValidatorForConflictingOptionality
                .flagIfConflict(
                        mandatoryFacet,
                        "Conflicting @Nullable with other optionality annotation"));

        // search for @Property(optional=...)
        addFacetIfPresent(
                MandatoryFacetForPropertyAnnotation
                .create(propertyIfAny, method, holder))
        .ifPresent(mandatoryFacet->
                MetaModelValidatorForConflictingOptionality
                .flagIfConflict(
                        mandatoryFacet,
                        "Conflicting Property#optionality with other optionality annotation"));
    }

    void processRegEx(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();
        val returnType = processMethodContext.getMethod().getReturnType();

        // check for @Pattern first
        val patternIfAny = processMethodContext.synthesizeOnMethod(Pattern.class);
        if (addFacetIfPresent(
                RegExFacetForPatternAnnotationOnProperty
                .create(patternIfAny, returnType, holder))
                .isPresent()) {
            return;
        }

        // else search for @Property(pattern=...)
        addFacetIfPresent(
                RegExFacetForPropertyAnnotation
                .create(propertyIfAny, returnType, holder));
    }

    void processFileAccept(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // else search for @Property(maxLength=...)
        addFacetIfPresent(
                FileAcceptFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

}
