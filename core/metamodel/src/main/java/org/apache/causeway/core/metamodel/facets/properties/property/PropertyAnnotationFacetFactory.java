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

import javax.inject.Inject;
import javax.validation.constraints.Pattern;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
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
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacetForClearing;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyModifyFacetForSetting;
import org.apache.causeway.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPatternAnnotationOnProperty;
import org.apache.causeway.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.property.snapshot.SnapshotExcludeFacetForPropertyAnnotation;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class PropertyAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public PropertyAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var propertyIfAny = propertyIfAny(processMethodContext);

        if(processMethodContext.isMixinMain()) {
            propertyIfAny.ifPresent(property->{
                inferMixinSort(property, processMethodContext.getFacetHolder());
            });
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
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), Property.class));
    }

    void inferMixinSort(final Property property, final FacetedMethod facetedMethod) {
        /* if @Property detected on method or type level infer:
         * @Action(semantics=SAFE) */
        addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        addFacet(ContributingFacetAbstract.createAsProperty(facetedMethod));
    }

    void processDomainEvent(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var cls = processMethodContext.getCls();
        var holder = processMethodContext.getFacetHolder();

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

        if(!isProperty) return; // bale out if method is not representing a property (no matter mixed-in or not)

        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //

        // search for @Property(domainEvent=...), else use default event type
        var propertyDomainEventFacet = PropertyDomainEventFacet
                .create(propertyIfAny, cls, getterFacetIfAny, holder);

        addFacet(propertyDomainEventFacet);

        getterFacetIfAny.ifPresent(getterFacet->{
            /* if the property is mutable (never true for mixed-in props),
             * then replace the current setter and clear facets with equivalents that
             * emit the appropriate domain event and then delegate onto the underlying */

            holder.lookupFacet(PropertySetterFacet.class)
            .ifPresent(setterFacet->
                    /* lazily binds the event-type to the propertyDomainEventFacet,
                     * such that any changes to the latter during post processing
                     * are reflected here as well
                     */
                    addFacet(new PropertyModifyFacetForSetting(
                            propertyDomainEventFacet, getterFacet, setterFacet, holder)));

            holder.lookupFacet(PropertyClearFacet.class)
            .ifPresent(clearFacet->
                    /* lazily binds the event-type to the propertyDomainEventFacet,
                     * such that any changes to the latter during post processing
                     * are reflected here as well
                     */
                    addFacet(new PropertyModifyFacetForClearing(
                            propertyDomainEventFacet, getterFacet, clearFacet, holder)));
        });

    }

    void processEditing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var facetHolder = processMethodContext.getFacetHolder();

        // search for @Property(editing=...)
        addFacetIfPresent(
                DisabledFacetForPropertyAnnotation
                .create(propertyIfAny, facetHolder));
    }

    void processCommandPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {
        var facetHolder = processMethodContext.getFacetHolder();

        // skip if a facet is already installed
        // (this is because - despite its name - this facet factory runs for both properties and actions;
        //  if the holder represents an action then an ExecutionPublishingFacet will already have been installed).
        if (facetHolder.containsNonFallbackFacet(CommandPublishingFacet.class)) {
            return;
        }

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(commandPublishing=...)
        addFacet(
                CommandPublishingFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), facetHolder,  getServiceInjector()));
    }

    void processProjecting(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var facetHolder = processMethodContext.getFacetHolder();

        addFacetIfPresent(
                ProjectingFacetFromPropertyAnnotation
                .create(propertyIfAny, facetHolder));

    }

    void processExecutionPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Property> propertyIfAny) {

        var holder = processMethodContext.getFacetHolder();

        // skip if a facet is already installed
        // (this is because - despite its name - this facet factory runs for both properties and actions;
        //  if the holder represents an action then an ExecutionPublishingFacet will already have been installed).
        if (holder.containsNonFallbackFacet(ExecutionPublishingFacet.class)) {
            return;
        }

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
        addFacet(
                ExecutionPublishingFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), holder));
    }

    void processMaxLength(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var holder = processMethodContext.getFacetHolder();

        // search for @Property(maxLength=...)
        addFacetIfPresent(
                MaxLengthFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.getFacetHolder();

        // search for @Property(mustSatisfy=...)
        addFacetIfPresent(
                MustSatisfySpecificationFacetForPropertyAnnotation
                .create(propertyIfAny, holder, getFactoryService()));
    }

    void processEntityPropertyChangePublishing(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.getFacetHolder();

        // search for @Property(entityPropertyChangePublishing=...)
        addFacetIfPresent(
                EntityPropertyChangePublishingPolicyFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processSnapshot(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.getFacetHolder();

        // search for @Property(notPersisted=...)
        addFacetIfPresent(
                SnapshotExcludeFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

    void processOptional(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {

        var method = processMethodContext.getMethod();
        var holder = processMethodContext.getFacetHolder();

        // check for @Nullable
        var hasNullable = method.isAnnotatedAsNullable();

        addFacetIfPresent(
                MandatoryFacetInvertedByNullableAnnotationOnProperty
                .create(hasNullable, method, holder));

        // search for @Property(optional=...)
        addFacetIfPresent(
                MandatoryFacetForPropertyAnnotation
                .create(propertyIfAny, method, holder));
    }

    void processRegEx(final ProcessMethodContext processMethodContext, final Optional<Property> propertyIfAny) {
        var holder = processMethodContext.getFacetHolder();
        var returnType = processMethodContext.getMethod().getReturnType();

        // check for @Pattern first
        var patternIfAny = processMethodContext.synthesizeOnMethod(Pattern.class);
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
        var holder = processMethodContext.getFacetHolder();

        // check for @Property(maxLength=...)
        addFacetIfPresent(
                FileAcceptFacetForPropertyAnnotation
                .create(propertyIfAny, holder));
    }

}
