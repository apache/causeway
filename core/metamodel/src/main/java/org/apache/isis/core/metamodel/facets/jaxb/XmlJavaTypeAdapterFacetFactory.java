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

package org.apache.isis.core.metamodel.facets.jaxb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.collect.Lists;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetForXmlRootElementAnnotation;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * just adds a validator
 */
public class XmlJavaTypeAdapterFacetFactory extends FacetFactoryAbstract
            implements MetaModelValidatorRefiner {

    public static final String ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_ABSTRACT =
            "isis.reflector.validator.jaxbViewModelNotAbstract";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_ABSTRACT_DEFAULT = true;

    public static final String ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_INNER_CLASS =
            "isis.reflector.validator.jaxbViewModelNotInnerClass";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_INNER_CLASS_DEFAULT = true;

    public static final String ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_PUBLIC_NO_ARG_CONSTRUCTOR =
            "isis.reflector.validator.jaxbViewModelNoArgConstructor";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_PUBLIC_NO_ARG_CONSTRUCTOR_DEFAULT = false;

    public static final String ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_REFERENCE_TYPE_ADAPTER =
            "isis.reflector.validator.jaxbViewModelReferenceTypeAdapter";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_REFERENCE_TYPE_ADAPTER_DEFAULT = true;

    public static final String ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_DATE_TIME_TYPE_ADAPTER =
            "isis.reflector.validator.jaxbViewModelDateTimeTypeAdapter";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_DATE_TIME_TYPE_ADAPTER_DEFAULT = true;

    public XmlJavaTypeAdapterFacetFactory() {
        super(FeatureType.OBJECTS_AND_PROPERTIES);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final XmlJavaTypeAdapter annotation = Annotations.getAnnotation(cls, XmlJavaTypeAdapter.class);
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processClassContext.getFacetHolder();
        final XmlJavaTypeAdapterFacetDefault facet = new XmlJavaTypeAdapterFacetDefault(holder,
                annotation.value(), getSpecificationLoader());

        FacetUtil.addFacet(facet);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processXmlJavaTypeAdapter(processMethodContext);
        processXmlTransient(processMethodContext);

    }

    private void processXmlJavaTypeAdapter(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();

        final XmlJavaTypeAdapter annotation = Annotations.getAnnotation(method, XmlJavaTypeAdapter.class);
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processMethodContext.getFacetHolder();
        final XmlJavaTypeAdapterFacetDefault facet = new XmlJavaTypeAdapterFacetDefault(holder,
                annotation.value(), getSpecificationLoader());

        FacetUtil.addFacet(facet);
    }

    private void processXmlTransient(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();

        final XmlTransient annotation = Annotations.getAnnotation(method, XmlTransient.class);
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processMethodContext.getFacetHolder();
        final XmlTransientFacet facet = new XmlTransientFacetDefault(holder);

        FacetUtil.addFacet(facet);
    }

    @Override
    public void refineMetaModelValidator(
            final MetaModelValidatorComposite metaModelValidator,
            final IsisConfiguration configuration) {

        final List<TypeValidator> typeValidators = getTypeValidators(configuration);
        final List<PropertyValidator> propertyValidators = getPropertyValidators(configuration);

        final MetaModelValidator validator = new MetaModelValidatorVisiting(
                new MetaModelValidatorVisiting.Visitor() {
                    @Override
                    public boolean visit(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {

                        validate(objectSpec, validationFailures);
                        return true;
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {

                        final boolean viewModel = objectSpec.isViewModel();
                        if(!viewModel) {
                            return;
                        }

                        final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
                        if (!(facet instanceof RecreatableObjectFacetForXmlRootElementAnnotation)) {
                            return;
                        }

                        for (final TypeValidator typeValidator : typeValidators) {
                            typeValidator.validate(objectSpec, validationFailures);
                        }

                        final List<OneToOneAssociation> properties = objectSpec.getProperties(Contributed.EXCLUDED);
                        for (final OneToOneAssociation property : properties) {
                            if (!property.containsDoOpFacet(PropertySetterFacet.class)) {
                                // ignore derived
                                continue;
                            }
                            for (final PropertyValidator adapterValidator : propertyValidators) {
                                adapterValidator.validate(objectSpec, property, validationFailures);
                            }
                        }
                    }
                });
        metaModelValidator.add(validator);
    }

    private List<TypeValidator> getTypeValidators(final IsisConfiguration configuration) {

        final List<TypeValidator> typeValidators = Lists.newArrayList();
        if(configuration.getBoolean(ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_ABSTRACT, ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_ABSTRACT_DEFAULT)) {
            typeValidators.add(new JaxbViewModelNotAbstractValidator());
        }
        if(configuration.getBoolean(ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_INNER_CLASS, ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_NOT_INNER_CLASS_DEFAULT)) {
            typeValidators.add(new JaxbViewModelNotInnerClassValidator());
        }
        if(configuration.getBoolean(ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_PUBLIC_NO_ARG_CONSTRUCTOR, ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_PUBLIC_NO_ARG_CONSTRUCTOR_DEFAULT)) {
            typeValidators.add(new JaxbViewModelPublicNoArgConstructorValidator());
        }
        return typeValidators;
    }

    private List<PropertyValidator> getPropertyValidators(final IsisConfiguration configuration) {
        final List<PropertyValidator> propertyValidators = Lists.newArrayList();
        if(configuration.getBoolean(ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_REFERENCE_TYPE_ADAPTER, ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_REFERENCE_TYPE_ADAPTER_DEFAULT)) {
            propertyValidators.add(new PropertyValidatorForReferenceTypes());
        }
        if(configuration.getBoolean(ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_DATE_TIME_TYPE_ADAPTER, ISIS_REFLECTOR_VALIDATOR_JAXB_VIEW_MODEL_DATE_TIME_TYPE_ADAPTER_DEFAULT)) {
            propertyValidators.add(new PropertyValidatorForDateTypes(java.sql.Timestamp.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.DateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDate.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDateTime.class));
            propertyValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalTime.class));
        }
        return propertyValidators;
    }

    private static abstract class TypeValidator {
        abstract void validate(
                final ObjectSpecification objectSpec,
                final ValidationFailures validationFailures);

    }

    private static abstract class PropertyValidator {
        abstract void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final ValidationFailures validationFailures);

    }

    private static class PropertyValidatorForReferenceTypes extends PropertyValidator {

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final ValidationFailures validationFailures) {

            final ObjectSpecification propertyTypeSpec = property.getSpecification();
            final Class<?> propertyType = propertyTypeSpec.getCorrespondingClass();
            if (!Persistable.class.isAssignableFrom(propertyType)) {
                return;
            }

            final XmlJavaTypeAdapterFacet xmlJavaTypeAdapterFacet =
                    propertyTypeSpec.getFacet(XmlJavaTypeAdapterFacet.class);
            if(xmlJavaTypeAdapterFacet != null) {
                return;
            }

            validationFailures.add("JAXB view model '%s' property '%s' is of type '%s' but that type is not annotated with @XmlJavaTypeAdapter.  The type must be annotated with @XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter.class) or equivalent.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    propertyType.getName());

        }
    }

    private static class PropertyValidatorForDateTypes extends PropertyValidator {
        private final Class<?> jodaType;

        private PropertyValidatorForDateTypes(final Class<?> jodaType) {
            this.jodaType = jodaType;
        }

        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final ValidationFailures validationFailures) {

            final ObjectSpecification propertyTypeSpec = property.getSpecification();
            final Class<?> propertyType = propertyTypeSpec.getCorrespondingClass();

            if (!jodaType.isAssignableFrom(propertyType)) {
                return;
            }

            final XmlJavaTypeAdapterFacet xmlJavaTypeAdapterFacet = property.getFacet(XmlJavaTypeAdapterFacet.class);
            if (xmlJavaTypeAdapterFacet != null) {
                return;
            }

            final XmlTransientFacet xmlTransientFacet =
                    property.getFacet(XmlTransientFacet.class);
            if(xmlTransientFacet != null) {
                return;
            }

            // else
            validationFailures.add("JAXB view model '%s' property '%s' is of type '%s' but is not annotated with @XmlJavaTypeAdapter.  The field/method must be annotated with @XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.XxxAdapter.ForJaxb.class) or equivalent, or be ignored by being annotated with @XmlTransient.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    jodaType.getName());
        }
    }

    private static class JaxbViewModelNotAbstractValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final ValidationFailures validationFailures) {

            if(objectSpec.isAbstract()) {
                validationFailures.add("JAXB view model '%s' is abstract", objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelNotInnerClassValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final ValidationFailures validationFailures) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            if(correspondingClass.isAnonymousClass()) {
                validationFailures.add("JAXB view model '%s' is an anonymous class", objectSpec.getFullIdentifier());
            } else if(correspondingClass.isLocalClass()) {
                validationFailures.add("JAXB view model '%s' is a local class", objectSpec.getFullIdentifier());
            } else if(correspondingClass.isMemberClass() && !Modifier.isStatic(correspondingClass.getModifiers())) {
                validationFailures.add("JAXB view model '%s' is an non-static inner class", objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelPublicNoArgConstructorValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final ValidationFailures validationFailures) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            final Constructor<?>[] constructors = correspondingClass.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                if(constructor.getParameterTypes().length == 0) {
                    if (!Modifier.isPublic(constructor.getModifiers())) {
                        validationFailures
                                .add("JAXB view model '%s' has a no-arg constructor, however it is not public",
                                        objectSpec.getFullIdentifier());
                    }
                    return;
                }
            }
            validationFailures.add("JAXB view model '%s' does not have a public no-arg constructor", objectSpec.getFullIdentifier());
        }
    }
}
