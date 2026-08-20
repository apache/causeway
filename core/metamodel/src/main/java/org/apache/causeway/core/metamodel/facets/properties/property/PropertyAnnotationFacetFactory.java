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
package org.apache.causeway.core.metamodel.facets.properties.property;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetedMethod;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.projection.ProjectingFacetFromPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.fileaccept.FileAcceptFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByNullableAnnotationOnProperty;
import org.apache.causeway.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.snapshot.SnapshotExcludeFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Pattern;

public class PropertyAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public PropertyAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        var propertyIfAny = propertyIfAny(processMethodContext);

        if(processMethodContext.featureType().isCollection()) {
            if(propertyIfAny.isPresent()) {
                // Property annotation is not allowed on collection feature
                ValidationFailureUtils
                    .raiseMemberInvalidAnnotation(processMethodContext.facetHolder(), Property.class);
            }
            return; // skip further processing, since this is a collection feature
        }

        if(propertyIfAny.isPresent()) {
            if(processMethodContext.isMixinMain()) {
                inferMixinSort(processMethodContext.facetHolder());
            } else if(processMethodContext.featureType().isAction()) {
                // Property annotation is not allowed on action feature (unless mixin main)
                ValidationFailureUtils
                    .raiseMemberInvalidAnnotation(processMethodContext.facetHolder(), Property.class);
            }
        }

        processDomainEvent(processMethodContext, propertyIfAny);
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

    Optional<Property> propertyIfAny(final ProcessMethodContext processMethodContext) {
        return processMethodContext
            .synthesizeOnMethodOrMixinType(
                    Property.class,
                    () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.facetHolder(), Property.class));
    }

    void inferMixinSort(final FacetedMethod facetedMethod) {
        /* if @Property detected on method or type level infer:
         * @Action(semantics=SAFE) */
        new ActionSemanticsFacet("InferSafeForMixedInProperty", SemanticsOf.SAFE, facetedMethod);
        ContributingFacetAbstract.createAsProperty(facetedMethod);
    }

    void processDomainEvent(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var holder = processMethodContext.facetHolder();

        /*
         * immutable properties as well as mixed-in ones have no setter, hence phases:
         * HIDE modifiable by events
         * DISABLE always disabled
         * VALIDATE n/a for events
         * EXECUTING n/a for events
         * EXECUTED n/a for events
         */

        var getterFacetIfAny = holder.lookupFacet(PropertyOrCollectionAccessorFacet.class);

        final boolean isProperty = getterFacetIfAny.isPresent()
                || (processMethodContext.isMixinMain()
                        && propertyIfAny.isPresent());

        if(!isProperty)
			return; // bale out if method is not representing a property (no matter mixed-in or not)

        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //

        // search for @Property(domainEvent=...), else use default event type
        var propertyDomainEventFacet = PropertyDomainEventFacet
                .create(propertyIfAny, processMethodContext, getterFacetIfAny);

        getterFacetIfAny.ifPresent(getterFacet->{
            /* if the property is mutable (never true for mixed-in props),
             * then replace the current setter and clear facets with equivalents that
             * emit the appropriate domain event and then delegate onto the underlying */

            holder.lookupFacet(PropertySetterFacet.class)
            .ifPresent(setterFacet->
                    /* binds the event-type to the propertyDomainEventFacet,
                     * such that any changes to the latter during post processing
                     * are reflected here as well
                     */
                    new PropertyModifyFacet(
                    		PropertySetterFacet.class,
                            propertyDomainEventFacet, getterFacet, setterFacet, holder));
        });

    }

    void processEditing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var facetHolder = processMethodContext.facetHolder();

        // search for @Property(editing=...)
        DisabledFacetForPropertyAnnotation
            .create(propertyIfAny, facetHolder);
    }

    void processCommandPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {
        var facetHolder = processMethodContext.facetHolder();

        // skip if a facet is already installed
        // (this is because - despite its name - this facet factory runs for both properties and actions;
        //  if the holder represents an action then an ExecutionPublishingFacet will already have been installed).
        if (facetHolder.containsNonFallbackFacet(CommandPublishingFacet.class))
			return;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.cls()))
			// do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;

        // check for @Property(commandPublishing=...)

        CommandPublishingFacetForPropertyAnnotation
            .create(propertyIfAny, getConfiguration(), facetHolder,  getServiceInjector());
    }

    void processProjecting(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var facetHolder = processMethodContext.facetHolder();

        ProjectingFacetFromPropertyAnnotation
            .create(propertyIfAny, facetHolder);
    }

    void processExecutionPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {

        var holder = processMethodContext.facetHolder();

        // skip if a facet is already installed
        // (this is because - despite its name - this facet factory runs for both properties and actions;
        //  if the holder represents an action then an ExecutionPublishingFacet will already have been installed).
        if (holder.containsNonFallbackFacet(ExecutionPublishingFacet.class))
			return;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.cls()))
			// do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;

        // check for @Property(executionPublishing=...)
        ExecutionPublishingFacetForPropertyAnnotation
            .create(propertyIfAny, getConfiguration(), holder);
    }

    void processMaxLength(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var holder = processMethodContext.facetHolder();

        // search for @Property(maxLength=...)
        MaxLengthFacetForPropertyAnnotation
                .create(propertyIfAny, holder);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.facetHolder();

        // search for @Property(mustSatisfy=...)
        MustSatisfySpecificationFacetForPropertyAnnotation
            .create(propertyIfAny, holder, getFactoryService());
    }

    void processEntityPropertyChangePublishing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.facetHolder();

        // search for @Property(entityPropertyChangePublishing=...)
        EntityPropertyChangePublishingPolicyFacetForPropertyAnnotation
            .create(propertyIfAny, holder);
    }

    void processSnapshot(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.facetHolder();

        // search for @Property(notPersisted=...)
        SnapshotExcludeFacetForPropertyAnnotation
            .create(propertyIfAny, holder);
    }

    void processOptional(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var method = processMethodContext.methodFacade();
        var holder = processMethodContext.facetHolder();

        // check for @Nullable
        var hasNullable = method.isAnnotatedAsNullable();

        MandatoryFacetInvertedByNullableAnnotationOnProperty
            .create(hasNullable, method, holder);

        // search for @Property(optional=...)
        MandatoryFacetForPropertyAnnotation
            .create(propertyIfAny, method, holder);
    }

    void processRegEx(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.facetHolder();
        var returnType = processMethodContext.methodFacade().getReturnType();

        // check for @Pattern first
        var patternIfAny = processMethodContext.synthesizeOnMethod(Pattern.class);
        if (RegExFacetForPatternAnnotationOnProperty
            	.create(patternIfAny, returnType, holder)
                .isPresent())
			return;

        // else search for @Property(pattern=...)
        RegExFacetForPropertyAnnotation
                .create(propertyIfAny, returnType, holder);
    }

    void processFileAccept(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.facetHolder();

        // check for @Property(maxLength=...)
        FileAcceptFacetForPropertyAnnotation
            .create(propertyIfAny, holder);
    }

}
