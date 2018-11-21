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

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Mandatory;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.MustSatisfy;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyInteraction;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
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
import org.apache.isis.core.metamodel.facets.objectvalue.regex.TitleFacetFormattedByRegex;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.property.command.CommandFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForDisabledAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.fileaccept.FileAcceptFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForHiddenAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForMandatoryAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByNullableAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByOptionalAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForMaxLengthAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForNotPersistedAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.publishing.PublishedPropertyFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForRegExAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForConflictingOptionality;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;
import org.apache.isis.core.metamodel.util.EventUtil;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForDeprecatedAnnotation postsPropertyChangedEventValidator = new MetaModelValidatorForDeprecatedAnnotation(PostsPropertyChangedEvent.class);
    private final MetaModelValidatorForDeprecatedAnnotation propertyInteractionValidator = new MetaModelValidatorForDeprecatedAnnotation(PropertyInteraction.class);
    private final MetaModelValidatorForDeprecatedAnnotation regexValidator = new MetaModelValidatorForDeprecatedAnnotation(RegEx.class);
    private final MetaModelValidatorForDeprecatedAnnotation optionalValidator = new MetaModelValidatorForDeprecatedAnnotation(Optional.class);
    private final MetaModelValidatorForDeprecatedAnnotation mandatoryValidator = new MetaModelValidatorForDeprecatedAnnotation(Mandatory.class);
    private final MetaModelValidatorForDeprecatedAnnotation hiddenValidator = new MetaModelValidatorForDeprecatedAnnotation(Hidden.class);
    private final MetaModelValidatorForDeprecatedAnnotation disabledValidator = new MetaModelValidatorForDeprecatedAnnotation(Disabled.class);
    private final MetaModelValidatorForDeprecatedAnnotation maxLengthValidator = new MetaModelValidatorForDeprecatedAnnotation(MaxLength.class);
    private final MetaModelValidatorForDeprecatedAnnotation mustSatisfyValidator = new MetaModelValidatorForDeprecatedAnnotation(MustSatisfy.class);
    private final MetaModelValidatorForDeprecatedAnnotation notPersistedValidator = new MetaModelValidatorForDeprecatedAnnotation(NotPersisted.class);
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

        //
        // Set up PropertyDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //
        final PostsPropertyChangedEvent postsPropertyChangedEvent = Annotations.getAnnotation(method, PostsPropertyChangedEvent.class);
        final PropertyInteraction propertyInteraction = Annotations.getAnnotation(method, PropertyInteraction.class);
        final Property property = Annotations.getAnnotation(method, Property.class);
        final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType;

        final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

        // can't really do this, because would result in the event being fired for the
        // hidden/disable/validate phases, most likely breaking existing code.
//        if(postsPropertyChangedEvent != null) {
//            propertyDomainEventType = postsPropertyChangedEvent.value();
//            propertyDomainEventFacet = postsPropertyChangedEventValidator.flagIfPresent(
//                    new PropertyDomainEventFacetForPostsPropertyChangedEventAnnotation(
//                        propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder));
//        } else

        // search for @PropertyInteraction(value=...)
        if(propertyInteraction != null) {
            propertyDomainEventType = defaultFromDomainObjectIfRequired(typeSpec, propertyInteraction.value());
            propertyDomainEventFacet = propertyInteractionValidator.flagIfPresent(
                    new PropertyDomainEventFacetForPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder), processMethodContext);
        } else
        // search for @Property(domainEvent=...)
        if(property != null) {
            propertyDomainEventType = defaultFromDomainObjectIfRequired(typeSpec, property.domainEvent());
            propertyDomainEventFacet = new PropertyDomainEventFacetForPropertyAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);

        } else
        // else use default event type
        {
            propertyDomainEventType = defaultFromDomainObjectIfRequired(typeSpec, PropertyDomainEvent.Default.class);
            propertyDomainEventFacet = new PropertyDomainEventFacetDefault(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);
        }

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
            final PropertyClearFacet replacementFacet;

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
            // default
            {
                replacementFacet = new PropertyClearFacetForDomainEventFromDefault(
                        propertyDomainEventType, getterFacet, clearFacet, propertyDomainEventFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }
    }

    private static Class<? extends PropertyDomainEvent<?,?>> defaultFromDomainObjectIfRequired(
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

        // check for deprecated @Hidden first
        final Hidden hiddenAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), Hidden.class);
        HiddenFacet facet = hiddenValidator.flagIfPresent(HiddenFacetForHiddenAnnotationOnProperty.create(hiddenAnnotation, holder), processMethodContext);

        // else search for @Property(hidden=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = HiddenFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @Disabled first
        final Disabled annotation = Annotations.getAnnotation(method, Disabled.class);
        final DisabledFacet disabledFacet = DisabledFacetForDisabledAnnotationOnProperty.create(annotation, holder);
        DisabledFacet facet = disabledValidator.flagIfPresent(disabledFacet, processMethodContext);

        // else search for @Property(editing=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = DisabledFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {

        final Class<?> cls = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final FacetHolder holder = facetHolder;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(command=...)
        final CommandFacet commandFacet = CommandFacetForPropertyAnnotation.create(property, getConfiguration(), holder,
                servicesInjector);

        FacetUtil.addFacet(commandFacet);
    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Property(publishing=...)
        final PublishedPropertyFacet facet = PublishedPropertyFacetForPropertyAnnotation
                .create(property, getConfiguration(), holder);

        FacetUtil.addFacet(facet);
    }



    void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @MaxLength first
        final MaxLength annotation = Annotations.getAnnotation(method, MaxLength.class);
        MaxLengthFacet facet = maxLengthValidator.flagIfPresent(MaxLengthFacetForMaxLengthAnnotationOnProperty.create(annotation, holder), processMethodContext);

        // else search for @Property(maxLength=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = MaxLengthFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @MustSatisfy first
        final MustSatisfy annotation = Annotations.getAnnotation(method, MustSatisfy.class);
        Facet facet = mustSatisfyValidator.flagIfPresent(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.create(annotation, holder, servicesInjector), processMethodContext);

        // else search for @Property(mustSatisfy=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(property, holder, servicesInjector);
        }

        FacetUtil.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @NotPersisted first
        final NotPersisted annotation = Annotations.getAnnotation(method, NotPersisted.class);
        NotPersistedFacet facet = notPersistedValidator.flagIfPresent(NotPersistedFacetForNotPersistedAnnotationOnProperty.create(annotation, holder), processMethodContext);

        // else search for @Property(notPersisted=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = NotPersistedFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processOptional(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();

        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @Optional
        final Optional optionalAnnotation = Annotations.getAnnotation(method, Optional.class);
        FacetUtil.addFacet(
                optionalValidator.flagIfPresent(
                    MandatoryFacetInvertedByOptionalAnnotationOnProperty.create(optionalAnnotation, method, holder),
                    processMethodContext));

        // check for deprecated @Mandatory
        final Mandatory mandatoryAnnotation = Annotations.getAnnotation(method, Mandatory.class);
        final MandatoryFacet facet =
                mandatoryValidator.flagIfPresent(
                    MandatoryFacetForMandatoryAnnotationOnProperty.create(mandatoryAnnotation, holder),
                    processMethodContext);
        FacetUtil.addFacet(facet);
        conflictingOptionalityValidator.flagIfConflict(
                facet, "Conflicting @Mandatory with other optionality annotation");

        // else check for @Nullable
        final Nullable nullableAnnotation = Annotations.getAnnotation(method, Nullable.class);
        final MandatoryFacet facet2 =
                MandatoryFacetInvertedByNullableAnnotationOnProperty.create(nullableAnnotation, method, holder);
        FacetUtil.addFacet(facet2);
        conflictingOptionalityValidator.flagIfConflict(
                    facet2, "Conflicting @Nullable with other optionality annotation");

        // else search for @Property(optional=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        final MandatoryFacet facet3 = MandatoryFacetForPropertyAnnotation.create(property, method, holder);
        FacetUtil.addFacet(facet3);
        conflictingOptionalityValidator.flagIfConflict(
                    facet3, "Conflicting Property#optionality with other optionality annotation");
    }

    void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();

        // check for deprecated @RegEx first
        final RegEx annotation = Annotations.getAnnotation(processMethodContext.getMethod(), RegEx.class);
        RegExFacet facet = regexValidator.flagIfPresent(RegExFacetForRegExAnnotationOnProperty.create(annotation, returnType, holder), processMethodContext);

        if (facet != null) {
            // @RegEx also supports corresponding title facet
            FacetUtil.addFacet(new TitleFacetFormattedByRegex(facet));
        }

        // else search for @Property(pattern=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if (facet == null) {
            facet = RegExFacetForPropertyAnnotation.create(property, returnType, holder);
        }

        FacetUtil.addFacet(facet);
    }


    void processFileAccept(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();


        // else search for @Property(maxLength=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        FileAcceptFacet facet = FileAcceptFacetForPropertyAnnotation.create(property, holder);

        FacetUtil.addFacet(facet);
    }


    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(postsPropertyChangedEventValidator);
        metaModelValidator.add(propertyInteractionValidator);
        metaModelValidator.add(regexValidator);
        metaModelValidator.add(optionalValidator);
        metaModelValidator.add(mandatoryValidator);
        metaModelValidator.add(hiddenValidator);
        metaModelValidator.add(disabledValidator);
        metaModelValidator.add(maxLengthValidator);
        metaModelValidator.add(mustSatisfyValidator);
        metaModelValidator.add(notPersistedValidator);
        metaModelValidator.add(conflictingOptionalityValidator);
    }

    // //////////////////////////////////////



    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = servicesInjector.getConfigurationServiceInternal();
        postsPropertyChangedEventValidator.setConfiguration(configuration);
        propertyInteractionValidator.setConfiguration(configuration);
        regexValidator.setConfiguration(configuration);
        optionalValidator.setConfiguration(configuration);
        mandatoryValidator.setConfiguration(configuration);
        hiddenValidator.setConfiguration(configuration);
        disabledValidator.setConfiguration(configuration);
        maxLengthValidator.setConfiguration(configuration);
        mustSatisfyValidator.setConfiguration(configuration);
        notPersistedValidator.setConfiguration(configuration);
    }

}
