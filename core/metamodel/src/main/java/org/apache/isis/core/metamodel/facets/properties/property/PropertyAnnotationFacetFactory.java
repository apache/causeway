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
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.annotprop.DisabledFacetAnnotation;
import org.apache.isis.core.metamodel.facets.members.hidden.annotprop.HiddenFacetOnMemberAnnotation;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.object.regex.TitleFacetFormattedByRegex;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForMandatoryAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForNotPersistedAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetInvertedByOptionalAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetFromRegExAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForMaxLengthAnnotationOnProperty;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, ContributeeMemberFacetFactory, MetaModelValidatorRefiner, IsisConfigurationAware {

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

    private ServicesInjector servicesInjector;

    public PropertyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processDomainEvent(processMethodContext);
        processHidden(processMethodContext);
        processEditing(processMethodContext);
        processMaxLength(processMethodContext);
        processMustSatisfy(processMethodContext);
        processNotPersisted(processMethodContext);
        processOptional(processMethodContext);
        processRegEx(processMethodContext);
    }

    void processDomainEvent(final ProcessMethodContext processMethodContext) {

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
        final PropertyInteraction propertyInteraction = Annotations.getAnnotation(method, PropertyInteraction.class);
        final Property property = Annotations.getAnnotation(method, Property.class);
        final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType;

        final PropertyDomainEventFacetAbstract propertyDomainEventFacet;

        // search for @PostsPropertyChanged(value=...)
        // (even though these do not participate in hide/disable/validate, we rely on the validate to set up the
        // event on a thread-local, such that it is then picked up later by the setter/clear facets
        if(postsPropertyChangedEvent != null) {
            propertyDomainEventType = postsPropertyChangedEvent.value();
            propertyDomainEventFacet = postsPropertyChangedEventValidator.flagIfPresent(
                    new PropertyDomainEventFacetForPostsPropertyChangedEventAnnotation(
                        propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder));
        } else
        // search for @PropertyInteraction(value=...)
        if(propertyInteraction != null) {
            propertyDomainEventType = propertyInteraction.value();
            propertyDomainEventFacet = propertyInteractionValidator.flagIfPresent(
                    new PropertyDomainEventFacetForPropertyInteractionAnnotation(
                        propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder));
        } else
        // search for @Property(domainEvent=...)
        if(property != null && property.domainEvent() != null) {
            propertyDomainEventType = property.domainEvent();
            propertyDomainEventFacet = new PropertyDomainEventFacetForPropertyAnnotation(
                    propertyDomainEventType, getterFacet, servicesInjector, getSpecificationLoader(), holder);

        } else
        // else use default event type
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
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @Hidden first
        final Hidden hiddenAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), Hidden.class);
        HiddenFacet facet = hiddenValidator.flagIfPresent(HiddenFacetOnMemberAnnotation.create(hiddenAnnotation, holder));

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
        DisabledFacet facet = disabledValidator.flagIfPresent(DisabledFacetAnnotation.create(annotation, holder));

        // else search for @Property(editing=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = DisabledFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @MaxLength first
        final MaxLength annotation = Annotations.getAnnotation(method, MaxLength.class);
        MaxLengthFacet facet = maxLengthValidator.addFacetFlagIfPresent(MaxLengthFacetForMaxLengthAnnotationOnProperty.create(annotation, holder));

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
        Facet facet = mustSatisfyValidator.addFacetFlagIfPresent(MustSatisfySpecificationFacetForMustSatisfyAnnotationOnProperty.create(annotation, holder));

        // else search for @Property(mustSatisfy=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = MustSatisfySpecificationFacetForPropertyAnnotation.create(property, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processNotPersisted(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @NotPersisted first
        final NotPersisted annotation = Annotations.getAnnotation(method, NotPersisted.class);
        NotPersistedFacet facet = notPersistedValidator.addFacetFlagIfPresent(NotPersistedFacetForNotPersistedAnnotationOnProperty.create(annotation, holder));

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

        // check for deprecated @Optional first
        final Optional annotation = Annotations.getAnnotation(method, Optional.class);
        MandatoryFacet facet = optionalValidator.flagIfPresent(MandatoryFacetInvertedByOptionalAnnotationOnProperty.create(annotation, method, holder));

        // else check for deprecated @Mandatory first
        Mandatory mandatoryAnnotation = Annotations.getAnnotation(method, Mandatory.class);
        if(facet == null) {
            facet = mandatoryValidator.flagIfPresent(MandatoryFacetForMandatoryAnnotationOnProperty.create(mandatoryAnnotation, holder));
        }

        // else search for @Property(optional=...)
        final Property property = Annotations.getAnnotation(method, Property.class);
        if(facet == null) {
            facet = MandatoryFacetForPropertyAnnotation.create(property, method, holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();

        // check for deprecated @RegEx first
        final RegEx annotation = Annotations.getAnnotation(processMethodContext.getMethod(), RegEx.class);
        RegExFacet facet = regexValidator.flagIfPresent(RegExFacetFromRegExAnnotationOnProperty.create(annotation, returnType, holder));

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

    // //////////////////////////////////////

    @Override
    public void process(final ContributeeMemberFacetFactory.ProcessContributeeMemberContext processMemberContext) {

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
    }

    // //////////////////////////////////////


    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
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


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
