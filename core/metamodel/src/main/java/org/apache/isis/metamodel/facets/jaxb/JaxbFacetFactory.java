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

package org.apache.isis.metamodel.facets.jaxb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.object.recreatable.RecreatableObjectFacetForXmlRootElementAnnotation;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

/**
 * just adds a validator
 */
public class JaxbFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {

    public JaxbFacetFactory() {
        super(FeatureType.OBJECTS_AND_PROPERTIES);
    }

    // -- CLASS CONTEXT

    @Override
    public void process(final ProcessClassContext processClassContext) {
        processXmlJavaTypeAdapter(processClassContext);
        processXmlAccessorTypeFacet(processClassContext);
    }

    private void processXmlJavaTypeAdapter(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final XmlJavaTypeAdapter annotation = Annotations.getAnnotation(cls, XmlJavaTypeAdapter.class);
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processClassContext.getFacetHolder();
        final XmlJavaTypeAdapterFacetDefault facet = new XmlJavaTypeAdapterFacetDefault(holder,
                annotation.value());

        super.addFacet(facet);
    }

    private void processXmlAccessorTypeFacet(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();

        final XmlAccessorType annotation = Annotations.getAnnotation(cls, XmlAccessorType.class);
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processClassContext.getFacetHolder();
        final XmlAccessorTypeFacetDefault facet =
                new XmlAccessorTypeFacetDefault(holder, annotation.value());

        super.addFacet(facet);
    }

    // -- METHOD CONTEXT

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        //[ahuber] accessType not yet used, but could be in future extensions
        final Optional<XmlAccessorTypeFacet> accessorTypeFacet =
                Optional.ofNullable(processMethodContext.getFacetHolder().getFacet(XmlAccessorTypeFacet.class));
        final XmlAccessType accessType = accessorTypeFacet
                .map(facet->facet.value())
                .orElse(XmlAccessType.PUBLIC_MEMBER); // the annotation's default value
        // ---

        processXmlJavaTypeAdapter(processMethodContext, accessType);
        processXmlTransient(processMethodContext, accessType);

    }

    private void processXmlJavaTypeAdapter(final ProcessMethodContext processMethodContext, XmlAccessType accessType) {
        //val method = processMethodContext.getMethod();
        
        final XmlJavaTypeAdapter annotation = processMethodContext.synthesizeOnMethod(XmlJavaTypeAdapter.class)
                .orElse(null);
                
//        _Assert.assertEquals("expected same", annotation,
//                Annotations.getAnnotation(method, XmlJavaTypeAdapter.class));
        
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processMethodContext.getFacetHolder();
        final XmlJavaTypeAdapterFacetDefault facet = new XmlJavaTypeAdapterFacetDefault(holder,
                annotation.value());

        super.addFacet(facet);
    }

    private void processXmlTransient(final ProcessMethodContext processMethodContext, XmlAccessType accessType) {
        //val method = processMethodContext.getMethod();

        final XmlTransient annotation = processMethodContext.synthesizeOnMethod(XmlTransient.class)
                .orElse(null);
                
//        _Assert.assertEquals("expected same", annotation,
//                Annotations.getAnnotation(method, XmlTransient.class));
        
        if(annotation == null) {
            return;
        }

        final FacetHolder holder = processMethodContext.getFacetHolder();
        final XmlTransientFacet facet = new XmlTransientFacetDefault(holder);

        super.addFacet(facet);
    }

    // --

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        final List<TypeValidator> typeValidators = getTypeValidators(getConfiguration());
        final List<PropertyValidator> propertyValidators = getPropertyValidators(getConfiguration());

        programmingModel.addValidator(
                new MetaModelValidatorVisiting.Visitor() {
                    @Override
                    public boolean visit(
                            final ObjectSpecification objectSpec,
                            final MetaModelValidator validator) {

                        validate(objectSpec, validator);
                        return true;
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final MetaModelValidator validator) {

                        final boolean viewModel = objectSpec.isViewModel();
                        if(!viewModel) {
                            return;
                        }

                        final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
                        if (!(facet instanceof RecreatableObjectFacetForXmlRootElementAnnotation)) {
                            return;
                        }

                        for (final TypeValidator typeValidator : typeValidators) {
                            typeValidator.validate(objectSpec, validator);
                        }

                        final Stream<OneToOneAssociation> properties = objectSpec
                                .streamProperties(Contributed.EXCLUDED);

                        properties
                        // ignore derived
                        .filter(property->property.containsNonFallbackFacet(PropertySetterFacet.class))
                        .forEach(property->{
                            for (final PropertyValidator adapterValidator : propertyValidators) {
                                adapterValidator.validate(objectSpec, property, validator);
                            }
                        });

                    }
                });
        
    }

    private List<TypeValidator> getTypeValidators(IsisConfiguration configuration) {

        final List<TypeValidator> typeValidators = _Lists.newArrayList();
        if(configuration.getReflector().getValidator().isJaxbViewModelNotAbstract()) {
            typeValidators.add(new JaxbViewModelNotAbstractValidator());
        }
        if(configuration.getReflector().getValidator().isJaxbViewModelNotInnerClass()) {
            typeValidators.add(new JaxbViewModelNotInnerClassValidator());
        }
        if(configuration.getReflector().getValidator().isJaxbViewModelNoArgConstructor()) {
            typeValidators.add(new JaxbViewModelPublicNoArgConstructorValidator());
        }
        return typeValidators;
    }

    private List<PropertyValidator> getPropertyValidators(IsisConfiguration configuration) {
        final List<PropertyValidator> propertyValidators = _Lists.newArrayList();
        if(configuration.getReflector().getValidator().isJaxbViewModelReferenceTypeAdapter()) {
            propertyValidators.add(new PropertyValidatorForReferenceTypes());
        }
        if(configuration.getReflector().getValidator().isJaxbViewModelDateTimeTypeAdapter()) {
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
                final MetaModelValidator validator);

    }

    private static abstract class PropertyValidator {
        abstract void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final MetaModelValidator validator);

    }

    private static class PropertyValidatorForReferenceTypes extends PropertyValidator {

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final MetaModelValidator validator) {

            final ObjectSpecification propertyTypeSpec = property.getSpecification();
            if (!propertyTypeSpec.isEntity()) {
                return;
            }

            final XmlJavaTypeAdapterFacet xmlJavaTypeAdapterFacet =
                    propertyTypeSpec.getFacet(XmlJavaTypeAdapterFacet.class);
            if(xmlJavaTypeAdapterFacet != null) {
                return;
            }
            final Class<?> propertyType = propertyTypeSpec.getCorrespondingClass();
            validator.onFailure(
                    property,
                    property.getIdentifier(),
                    "JAXB view model '%s' property '%s' is of type '%s' but that type is not annotated with @XmlJavaTypeAdapter.  The type must be annotated with @XmlJavaTypeAdapter(org.apache.isis.applib.jaxbadapters.PersistentEntityAdapter.class) or equivalent.",
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

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final OneToOneAssociation property,
                final MetaModelValidator validator) {

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
            validator.onFailure(
                    property,
                    property.getIdentifier(),
                    "JAXB view model '%s' property '%s' is of type '%s' but is not annotated with @XmlJavaTypeAdapter.  The field/method must be annotated with @XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.XxxAdapter.ForJaxb.class) or equivalent, or be ignored by being annotated with @XmlTransient.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    jodaType.getName());
        }
    }

    private static class JaxbViewModelNotAbstractValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final MetaModelValidator validator) {

            if(objectSpec.isAbstract()) {
                validator.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "JAXB view model '%s' is abstract", 
                        objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelNotInnerClassValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final MetaModelValidator validator) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            if(correspondingClass.isAnonymousClass()) {
                validator.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "JAXB view model '%s' is an anonymous class", 
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isLocalClass()) {
                validator.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "JAXB view model '%s' is a local class", 
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isMemberClass() && !Modifier.isStatic(correspondingClass.getModifiers())) {
                validator.onFailure(
                        objectSpec,
                        objectSpec.getIdentifier(),
                        "JAXB view model '%s' is an non-static inner class", 
                        objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelPublicNoArgConstructorValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final MetaModelValidator validator) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            final Constructor<?>[] constructors = correspondingClass.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                if(constructor.getParameterCount() == 0) {
                    if (!MethodUtil.isPublic(constructor)) {
                        validator.onFailure(
                                objectSpec,
                                objectSpec.getIdentifier(),
                                "JAXB view model '%s' has a no-arg constructor, however it is not public",
                                objectSpec.getFullIdentifier());
                    }
                    return;
                }
            }
            validator.onFailure(
                    objectSpec,
                    objectSpec.getIdentifier(),
                    "JAXB view model '%s' does not have a public no-arg constructor", 
                    objectSpec.getFullIdentifier());
        }
    }
}
