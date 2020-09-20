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
import javax.validation.constraints.Pattern;

import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
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
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract 
implements MetaModelRefiner {

    private final MetaModelValidatorForConflictingOptionality conflictingOptionalityValidator = 
            new MetaModelValidatorForConflictingOptionality();

    public PropertyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_AND_ACTIONS);
    }
    
    @Override
    public void setMetaModelContext(MetaModelContext metaModelContext) {
        super.setMetaModelContext(metaModelContext);
        conflictingOptionalityValidator.setMetaModelContext(metaModelContext);
    }
    
    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        
        val propertyIfAny = processMethodContext.synthesizeOnMethodOrMixinType(Property.class);
        
        inferIntentWhenOnTypeLevel(processMethodContext, propertyIfAny);
        
        processModify(processMethodContext, propertyIfAny);
        processHidden(processMethodContext, propertyIfAny);
        processEditing(processMethodContext, propertyIfAny);
        processCommand(processMethodContext, propertyIfAny);
        processProjecting(processMethodContext, propertyIfAny);
        processPublishing(processMethodContext, propertyIfAny);
        processMaxLength(processMethodContext, propertyIfAny);
        processMustSatisfy(processMethodContext, propertyIfAny);
        processNotPersisted(processMethodContext, propertyIfAny);
        processOptional(processMethodContext, propertyIfAny);
        processRegEx(processMethodContext, propertyIfAny);
        processFileAccept(processMethodContext, propertyIfAny);
    }


    void inferIntentWhenOnTypeLevel(ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
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
        FacetUtil.addOrReplaceFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        FacetUtil.addFacet(new NotContributedFacetAbstract(Contributed.AS_ASSOCIATION, facetedMethod) {});
    }

    void processModify(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {

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
            super.addFacet(propertyDomainEventFacet);
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
            super.addFacet(replacementFacet);
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
            super.addFacet(replacementFacet);
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



    void processHidden(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();
        
        // search for @Property(hidden=...)
        val hiddenFacet = HiddenFacetForPropertyAnnotation.create(propertyIfAny, facetHolder);

        super.addFacet(hiddenFacet);
    }

    void processEditing(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();

        // search for @Property(editing=...)
        val disabledFacet = DisabledFacetForPropertyAnnotation.create(propertyIfAny, facetHolder);

        super.addFacet(disabledFacet);
    }

    void processCommand(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val facetHolder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasUniqueId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(command=...)
        val commandFacet = CommandFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), facetHolder,  getServiceInjector());

        super.addFacet(commandFacet);
    }

    void processProjecting(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {

        val facetHolder = processMethodContext.getFacetHolder();

        val projectingFacet = ProjectingFacetFromPropertyAnnotation
                .create(propertyIfAny, facetHolder);

        super.addFacet(projectingFacet);

    }

    void processPublishing(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {

        
        val holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasUniqueId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(publishing=...)
        val facet = PublishedPropertyFacetForPropertyAnnotation
                .create(propertyIfAny, getConfiguration(), holder);

        super.addFacet(facet);
    }



    void processMaxLength(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {

        val holder = processMethodContext.getFacetHolder();

        // search for @Property(maxLength=...)
        val facet = MaxLengthFacetForPropertyAnnotation.create(propertyIfAny, holder);

        super.addFacet(facet);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(mustSatisfy=...)
        val facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(propertyIfAny, holder, getFactoryService());

        super.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Property(notPersisted=...)
        val facet = NotPersistedFacetForPropertyAnnotation.create(propertyIfAny, holder);

        super.addFacet(facet);
    }

    void processOptional(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {

        val method = processMethodContext.getMethod();

        val holder = processMethodContext.getFacetHolder();

        // check for @Nullable
        val nullableIfAny = processMethodContext.synthesizeOnMethod(Nullable.class);
        val facet2 =
                MandatoryFacetInvertedByNullableAnnotationOnProperty.create(nullableIfAny, method, holder);
        super.addFacet(facet2);
        conflictingOptionalityValidator.flagIfConflict(
                facet2, "Conflicting @Nullable with other optionality annotation");

        // search for @Property(optional=...)
        val facet3 = MandatoryFacetForPropertyAnnotation.create(propertyIfAny, method, holder);
        super.addFacet(facet3);
        conflictingOptionalityValidator.flagIfConflict(
                facet3, "Conflicting Property#optionality with other optionality annotation");
    }

    void processRegEx(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();
        val returnType = processMethodContext.getMethod().getReturnType();

        // check for @Pattern first
        val patternIfAny = processMethodContext.synthesizeOnMethod(Pattern.class);
        val facet = RegExFacetForPatternAnnotationOnProperty.create(patternIfAny, returnType, holder);

        if (facet != null) {
            super.addFacet(facet);
            return;
        }
        
        // else search for @Property(pattern=...)
        val facet2 = RegExFacetForPropertyAnnotation.create(propertyIfAny, returnType, holder);
        super.addFacet(facet2);
        
        
    }


    void processFileAccept(final ProcessMethodContext processMethodContext, Optional<Property> propertyIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // else search for @Property(maxLength=...)
        val facet = FileAcceptFacetForPropertyAnnotation.create(propertyIfAny, holder);

        super.addFacet(facet);
    }

    // //////////////////////////////////////

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(conflictingOptionalityValidator);
    }


}
