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
public class JaxbXmlJavaTypeAdapterFacetFactory extends FacetFactoryAbstract
            implements MetaModelValidatorRefiner {

    public JaxbXmlJavaTypeAdapterFacetFactory() {
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

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {

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

                        for (final TypeValidator typeValidator : TYPE_VALIDATORS) {
                            typeValidator.validate(objectSpec, validationFailures);
                        }

                        final List<OneToOneAssociation> properties = objectSpec.getProperties(Contributed.EXCLUDED);
                        for (final OneToOneAssociation property : properties) {
                            for (final PropertyValidator adapterValidator : PROPERTY_VALIDATORS) {
                                adapterValidator.validate(objectSpec, property, validationFailures);
                            }
                        }
                    }
                });
        metaModelValidator.add(validator);
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

            // else
            validationFailures.add("JAXB view model '%s' property '%s' is of type '%s' but is not annotated with @XmlJavaTypeAdapter.  The field/method must be annotated with @XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.XxxAdapter.ForJaxb.class) or equivalent.",
                    objectSpec.getFullIdentifier(),
                    property.getId(),
                    jodaType.getName());
        }
    }

    private final static List<TypeValidator> TYPE_VALIDATORS =
            Lists.newArrayList(
                    new TypeValidator() {
                        @Override
                        void validate(
                                final ObjectSpecification objectSpec,
                                final ValidationFailures validationFailures) {

                            if(objectSpec.isAbstract()) {
                                validationFailures.add("JAXB view model '%s' is abstract", objectSpec.getFullIdentifier());
                            }
                        }
                    },
                    new TypeValidator() {
                        @Override
                        void validate(
                                final ObjectSpecification objectSpec,
                                final ValidationFailures validationFailures) {

                            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
                            if(correspondingClass.isAnonymousClass() || correspondingClass.isLocalClass() || correspondingClass.isMemberClass()) {
                                validationFailures.add("JAXB view model '%s' is an inner class", objectSpec.getFullIdentifier());
                            }
                        }
                    },
                    new TypeValidator() {
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
            );

    private final static List<PropertyValidator> PROPERTY_VALIDATORS =
            Lists.newArrayList(
                    new PropertyValidatorForReferenceTypes(),
                    new PropertyValidatorForDateTypes(java.sql.Timestamp.class),
                    new PropertyValidatorForDateTypes(org.joda.time.DateTime.class),
                    new PropertyValidatorForDateTypes(org.joda.time.LocalDate.class),
                    new PropertyValidatorForDateTypes(org.joda.time.LocalDateTime.class),
                    new PropertyValidatorForDateTypes(org.joda.time.LocalTime.class)
            );

}
