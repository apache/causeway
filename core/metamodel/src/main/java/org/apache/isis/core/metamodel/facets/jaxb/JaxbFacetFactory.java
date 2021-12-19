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

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetForXmlRootElementAnnotation;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.isPublic;
import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * just adds a validator
 */
public class JaxbFacetFactory
extends FacetFactoryAbstract
implements MetaModelRefiner {

    @Inject
    public JaxbFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_AND_PROPERTIES);
    }

    // -- CLASS CONTEXT

    @Override
    public void process(final ProcessClassContext processClassContext) {
        processXmlJavaTypeAdapter(processClassContext);
        processXmlAccessorTypeFacet(processClassContext);
    }

    private void processXmlJavaTypeAdapter(final ProcessClassContext processClassContext) {

        val xmlJavaTypeAdapterIfAny = processClassContext.synthesizeOnType(XmlJavaTypeAdapter.class);
        if(!xmlJavaTypeAdapterIfAny.isPresent()) {
            return;
        }

        val facetHolder = processClassContext.getFacetHolder();

        addFacet(
                new XmlJavaTypeAdapterFacetDefault(facetHolder, xmlJavaTypeAdapterIfAny.get().value()));
    }

    private void processXmlAccessorTypeFacet(final ProcessClassContext processClassContext) {

        val xmlAccessorTypeIfAny = processClassContext.synthesizeOnType(XmlAccessorType.class);
        if(!xmlAccessorTypeIfAny.isPresent()) {
            return;
        }

        val facetHolder = processClassContext.getFacetHolder();
        addFacet(
                new XmlAccessorTypeFacetDefault(facetHolder, xmlAccessorTypeIfAny.get().value()));
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

    private void processXmlJavaTypeAdapter(final ProcessMethodContext processMethodContext, final XmlAccessType accessType) {

        val xmlJavaTypeAdapterIfAny = processMethodContext.synthesizeOnMethod(XmlJavaTypeAdapter.class);

        if(!xmlJavaTypeAdapterIfAny.isPresent()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        addFacet(
                new XmlJavaTypeAdapterFacetDefault(facetHolder, xmlJavaTypeAdapterIfAny.get().value()));
    }

    private void processXmlTransient(final ProcessMethodContext processMethodContext, final XmlAccessType accessType) {

        val xmlTransientIfAny = processMethodContext.synthesizeOnMethod(XmlTransient.class);

        if(!xmlTransientIfAny.isPresent()) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        addFacet(new XmlTransientFacetDefault(facetHolder));
    }

    // --

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        final List<TypeValidator> typeValidators = getTypeValidators(getConfiguration());
        final List<AssociationValidator> associationValidators = getAssociationValidators(getConfiguration());

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec->{

            final boolean viewModel = objectSpec.isViewModel();
            if(!viewModel) {
                return;
            }

            final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
            if (!(facet instanceof RecreatableObjectFacetForXmlRootElementAnnotation)) {
                return;
            }

            for (final TypeValidator typeValidator : typeValidators) {
                typeValidator.validate(objectSpec);
            }

            final Stream<ObjectAssociation> associations = objectSpec
                    .streamAssociations(MixedIn.EXCLUDED);

            associations
            // ignore derived
            .filter(association->association.containsNonFallbackFacet(PropertySetterFacet.class))
            .forEach(association->{
                for (final AssociationValidator adapterValidator : associationValidators) {
                    adapterValidator.validate(objectSpec, association);
                }
            });

        });

    }

    private List<TypeValidator> getTypeValidators(final IsisConfiguration configuration) {

        final List<TypeValidator> typeValidators = _Lists.newArrayList();
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNotAbstract()) {
            typeValidators.add(new JaxbViewModelNotAbstractValidator());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNotInnerClass()) {
            typeValidators.add(new JaxbViewModelNotInnerClassValidator());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isNoArgConstructor()) {
            typeValidators.add(new JaxbViewModelPublicNoArgConstructorValidator());
        }
        return typeValidators;
    }

    private List<AssociationValidator> getAssociationValidators(final IsisConfiguration configuration) {
        final List<AssociationValidator> associationValidators = _Lists.newArrayList();
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isReferenceTypeAdapter()) {
            associationValidators.add(new PropertyValidatorForReferenceTypes());
        }
        if(configuration.getCore().getMetaModel().getValidator().getJaxbViewModel().isDateTimeTypeAdapter()) {
            associationValidators.add(new PropertyValidatorForDateTypes(java.sql.Timestamp.class));
            associationValidators.add(new PropertyValidatorForDateTypes(ZonedDateTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(OffsetDateTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(LocalDate.class));
            associationValidators.add(new PropertyValidatorForDateTypes(LocalDateTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(LocalTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(org.joda.time.DateTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDate.class));
            associationValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalDateTime.class));
            associationValidators.add(new PropertyValidatorForDateTypes(org.joda.time.LocalTime.class));
        }
        return associationValidators;
    }

    private static abstract class TypeValidator {
        abstract void validate(ObjectSpecification objectSpec);

    }

    private static abstract class AssociationValidator {
        abstract void validate(
                ObjectSpecification objectSpec,
                ObjectAssociation propertyOrCollection);

    }

    private static class PropertyValidatorForReferenceTypes extends AssociationValidator {

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final ObjectAssociation propertyOrCollection) {

            // when referencing an entity type, either the association or the entity type
            // must be annotated with @XmlJavaTypeAdapter, unless the association is marked
            // transient with @XmlTransientFacet
            val elementTypeSpec = propertyOrCollection.getElementType();
            if (elementTypeSpec.isEntity()
                    && !propertyOrCollection.containsFacet(XmlJavaTypeAdapterFacet.class)
                    && !propertyOrCollection.containsFacet(XmlTransientFacet.class)
                    && !elementTypeSpec.containsFacet(XmlJavaTypeAdapterFacet.class)) {

                val elementType = elementTypeSpec.getCorrespondingClass();
                ValidationFailure.raiseFormatted(
                        propertyOrCollection,
                        "JAXB view model '%s' %s '%s' is of entity type '%s', "
                        + "but is not annotated with @XmlJavaTypeAdapter. "
                        + "The referenced entity types must be annotated with "
                        + "@XmlJavaTypeAdapter(org.apache.isis.applib.jaxb.%s.class) or equivalent.",
                        objectSpec.getFullIdentifier(),
                        elementTypeSpec.isScalar()
                            ? "@Property"
                            : "@Collection",
                        propertyOrCollection.getId(),
                        elementType.getName(),
                        elementTypeSpec.isScalar()
                            ? "PersistentEntityAdapter"
                            : "PersistentEntitiesAdapter");
            }

        }
    }

    @RequiredArgsConstructor
    private static class PropertyValidatorForDateTypes extends AssociationValidator {
        private final Class<?> dateType;

        @Override
        void validate(
                final ObjectSpecification objectSpec,
                final ObjectAssociation propertyOrCollection) {

            val elementTypeSpec = propertyOrCollection.getElementType();
            val elementType = elementTypeSpec.getCorrespondingClass();
            if (dateType.isAssignableFrom(elementType)
                    && !propertyOrCollection.containsFacet(XmlJavaTypeAdapterFacet.class)
                    && !propertyOrCollection.containsFacet(XmlTransientFacet.class)) {

                ValidationFailure.raiseFormatted(
                        propertyOrCollection,
                        "JAXB view model '%s' %s '%s' is of type '%s', "
                        + "but is not annotated with @XmlJavaTypeAdapter. "
                        + "The field/method must be annotated with "
                        + "@XmlJavaTypeAdapter(org.apache.isis.schema.utils.jaxbadapters.XxxAdapter.ForJaxb.class) "
                        + "or equivalent, "
                        + "or be ignored by being annotated with @XmlTransient.",
                        objectSpec.getFullIdentifier(),
                        elementTypeSpec.isScalar()
                            ? "@Property"
                            : "@Collection",
                        propertyOrCollection.getId(),
                        dateType.getName());

            }
        }
    }

    private static class JaxbViewModelNotAbstractValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec) {

            if(objectSpec.isAbstract()) {
                ValidationFailure.raise(
                        objectSpec,
                        String.format("JAXB view model '%s' is abstract", objectSpec.getFullIdentifier())
                        );
            }
        }
    }

    private static class JaxbViewModelNotInnerClassValidator extends TypeValidator {
        @Override
        void validate(
                final ObjectSpecification objectSpec) {

            final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
            if(correspondingClass.isAnonymousClass()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is an anonymous class",
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isLocalClass()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is a local class",
                        objectSpec.getFullIdentifier());
            } else if(correspondingClass.isMemberClass() && !Modifier.isStatic(correspondingClass.getModifiers())) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' is an non-static inner class",
                        objectSpec.getFullIdentifier());
            }
        }
    }

    private static class JaxbViewModelPublicNoArgConstructorValidator extends TypeValidator {
        @Override
        void validate(final ObjectSpecification objectSpec) {

            val correspondingClass = objectSpec.getCorrespondingClass();

            val publicNoArgConstructors = _Reflect
                    .getPublicConstructors(correspondingClass)
                    .filter(paramCount(0));

            if(publicNoArgConstructors.getCardinality().isOne()) {
                return; // happy case
            }

            val privateNoArgConstructors = _Reflect
                    .getDeclaredConstructors(correspondingClass)
                    .filter(paramCount(0).and(isPublic().negate()));

            if(privateNoArgConstructors.isNotEmpty()) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' has a no-arg constructor, however it is not public",
                        objectSpec.getFullIdentifier());
            } else {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "JAXB view model '%s' does not have a public no-arg constructor",
                        objectSpec.getFullIdentifier());
            }
        }
    }
}
