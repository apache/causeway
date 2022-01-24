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
package org.apache.isis.testdomain.domainmodel;

import java.lang.annotation.Annotation;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.Validation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.bad.AmbiguousMixinAnnotations;
import org.apache.isis.testdomain.model.bad.AmbiguousTitle;
import org.apache.isis.testdomain.model.bad.Configuration_usingInvalidDomain;
import org.apache.isis.testdomain.model.bad.InvalidActionOverloading;
import org.apache.isis.testdomain.model.bad.InvalidContradictingTypeSemantics;
import org.apache.isis.testdomain.model.bad.InvalidDomainObjectOnInterface;
import org.apache.isis.testdomain.model.bad.InvalidLogicalTypeNameClash;
import org.apache.isis.testdomain.model.bad.InvalidMemberOverloadingWhenInherited;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedActionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedCollectionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedPropertySupport;
import org.apache.isis.testdomain.model.bad.InvalidPropertyAnnotationOnAction;
import org.apache.isis.testdomain.model.bad.OrphanedMemberSupportDetection;
import org.apache.isis.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.isis.testing.integtestsupport.applib.validate.DomainModelValidator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingInvalidDomain.class
        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class DomainModelTest_usingBadDomain {

    @Inject private IsisConfiguration configuration;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private DomainObjectTesterFactory testerFactory;

    private DomainModelValidator validator;

    @BeforeEach
    void setup() {
        validator = new DomainModelValidator(specificationLoader, configuration, isisSystemEnvironment);
        assertThrows(DomainModelException.class, validator::throwIfInvalid);
    }


    @Test
    void fullIntrospection_shouldBeEnabledByThisTestClass() {
        assertTrue(IntrospectionMode.isFullIntrospect(configuration, isisSystemEnvironment));
    }

    @Test
    void ambiguousTitle_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(AmbiguousTitle.class)),
                "conflict for determining a strategy for retrieval of title");
    }

    @Test
    void orphanedActionSupport_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedActionSupport.class)),
                validationMessage(
                        "InvalidOrphanedActionSupport",
                        "hideOrphaned()"));

        val tester = testerFactory.objectTester(InvalidOrphanedActionSupport.class);

        tester.assertValidationFailureOnMember(
                ProgrammingModelConstants.Validation.ORPHANED_METHOD, "hideMe()");
    }


    @Test
    void orphanedPropertySupport_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedPropertySupport.class)),
                validationMessage(
                        "InvalidOrphanedPropertySupport",
                        "hideMyProperty()"));

        val tester = testerFactory.objectTester(InvalidOrphanedPropertySupport.class);

        tester.assertValidationFailureOnMember(
                ProgrammingModelConstants.Validation.ORPHANED_METHOD, "hideMe()");
    }

    @Test
    void orphanedCollectionSupport_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedCollectionSupport.class)),
                validationMessage(
                        "InvalidOrphanedCollectionSupport",
                        "hideMyCollection()"));

        val tester = testerFactory.objectTester(InvalidOrphanedCollectionSupport.class);

        tester.assertValidationFailureOnMember(
                ProgrammingModelConstants.Validation.ORPHANED_METHOD, "hideMe()");
    }

    @Test
    void actionOverloading_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidActionOverloading.class)),
                "Action method overloading is not allowed");

        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(
                        InvalidMemberOverloadingWhenInherited.WhenAnnotationOptional.class)),
                "Action method overloading is not allowed");

        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(
                        InvalidMemberOverloadingWhenInherited.WhenAnnotationRequired.class)),
                "Action method overloading is not allowed");

        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(
                        InvalidMemberOverloadingWhenInherited.WhenEncapsulationEnabled.class)),
                "Action method overloading is not allowed");

//TODO should fail as well, but rather difficult to implement
//        validator.assertAnyFailuresContaining(
//                Identifier.classIdentifier(LogicalType.fqcn(
//                        InvalidMemberOverloadingWhenInherited.WhenAnnotationOptional.class)),
//                "#isActive(): has synthesized (effective) annotation @Domain.Include, is assumed to support a property");

        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(
                        InvalidMemberOverloadingWhenInherited.WhenAnnotationRequired.class)),
                validationMessage(
                        "",
                        "isActive()"));

        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(
                        InvalidMemberOverloadingWhenInherited.WhenEncapsulationEnabled.class)),
                validationMessage(
                        "",
                        "isActive()"));
    }

    @Test
    void logicalTypeNameClash_shouldFail() {
        assertLogicalTypeNameClashesAmong(Can.of(
                InvalidLogicalTypeNameClash.VariantA.class,
                InvalidLogicalTypeNameClash.VariantB.class

                //FIXME ISIS-2871 for some reason the value type c does not get considered
                //,InvalidLogicalTypeNameClash.VariantC.class
                ));
    }

    private void assertLogicalTypeNameClashesAmong(final Can<Class<?>> types) {

        val typeLiteralList = types.stream()
                .map(t->t.getName())
                .collect(Collectors.joining(", "));

        val classIdentifiers = types.stream()
                .map(t->Identifier.classIdentifier(LogicalType.fqcn(t)))
                .collect(Can.toCan());

        validator.assertAnyOfContainingAnyFailures(
                classIdentifiers,
                "Logical type name 'isis.testdomain.InvalidLogicalTypeNameClash' "
                        + "mapped to multiple non-abstract classes:\n"
                        + typeLiteralList);
    }

    @Test
    void contradictingTypeSemantics_shouldFailValidation() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidContradictingTypeSemantics.class)),
                "Cannot use @DomainObject and @Value on the same type: "
                + InvalidContradictingTypeSemantics.class.getName());
    }

    @ParameterizedTest
    @MethodSource("provideAmbiguousMixins")
    void ambiguousMixinAnnotions_shouldFailValidation(
            final Class<?> mixinClass,
            final Class<? extends Annotation> annotationType,
            final String mixinMethodName) {

        final String annotationLiteral = "@" + annotationType.getSimpleName();
        validator.assertAnyFailuresContaining(
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(mixinClass), mixinMethodName),
                String.format("Annotation %s on both method and type level is not allowed", annotationLiteral));
    }

    private static Stream<Arguments> provideAmbiguousMixins() {
        return Stream.of(
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinA.class, Action.class, "act"),
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinAL.class, ActionLayout.class, "act"),
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinP.class, Property.class, "prop"),
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinPL.class, PropertyLayout.class, "prop"),
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinC.class, Collection.class, "coll"),
          Arguments.of(AmbiguousMixinAnnotations.InvalidMixinCL.class, CollectionLayout.class, "coll")
        );
    }

    @Test
    void invalidDomainObjectOnInterface_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidDomainObjectOnInterface.class)),
                "Cannot use @DomainObject on interface:");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            OrphanedMemberSupportDetection.WhenEncapsulationEnabled.class,
            OrphanedMemberSupportDetection.WhenAnnotationRequired.class,
            OrphanedMemberSupportDetection.WhenAnnotationOptional.class
            })
    void orphanedMemberSupportDiscovery(final Class<?> classUnderTest) {

        val clsIdUnderTest = Identifier.classIdentifier(LogicalType.fqcn(classUnderTest));

        // namedPlaceOrder(): String = "my name"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "namedPlaceOrder");

        // describedPlaceOrder(): String = "my description"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "describedPlaceOrder");

        // hidePlaceOrder(): boolean = false
        validator.assertAnyFailuresContaining(clsIdUnderTest, "hidePlaceOrder");

        // disablePlaceOrder(): String = "my disable reason"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "disablePlaceOrder");

        // default0PlaceOrder(): String = "my default-0"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "default0PlaceOrder");
        // default1PlaceOrder(): String = "my default-1"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "default1PlaceOrder");

        // hide0PlaceOrder(x): boolean = true
        validator.assertAnyFailuresContaining(clsIdUnderTest, "hide0PlaceOrder");
        // hide1PlaceOrder(y): boolean = false
        validator.assertAnyFailuresContaining(clsIdUnderTest, "hide1PlaceOrder");

        // disable0PlaceOrder(x): String = "my disable reason-0"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "disable0PlaceOrder");
        // disable1PlaceOrder(z): String = "my disable reason-1"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "disable1PlaceOrder");

        // choices0PlaceOrder(x): List.of("my choice")
        validator.assertAnyFailuresContaining(clsIdUnderTest, "choices0PlaceOrder");

        // autoComplete1PlaceOrder(y, search): List.of("my search arg=" + search)
        validator.assertAnyFailuresContaining(clsIdUnderTest, "autoComplete1PlaceOrder");

        // validate0PlaceOrder(String x): String = "my validation-0"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "validate0PlaceOrder");
        // validate1PlaceOrder(String y): String = "my validation-1"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "validate1PlaceOrder");
        // validatePlaceOrder(String x, final String y): String = "my validation"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "validatePlaceOrder");

        // -- PROPERTY

        // namedEmail(): String = "my email"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "namedEmail");

        // describedEmail: String = "my email described"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "describedEmail");

        // hideEmail(): boolean = true
        validator.assertAnyFailuresContaining(clsIdUnderTest, "hideEmail");

        // disableEmail(): String = "my email disable"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "disableEmail");

        // defaultEmail(): String = "my default email"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "defaultEmail");

        // choicesEmail(): Collection<String> = List.of("my email choice")
        validator.assertAnyFailuresContaining(clsIdUnderTest, "choicesEmail");

        // validateEmail(final String email): String = "my email validate"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "validateEmail");

        // -- COLLECTION

        // namedOrders(): String = "my orders"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "namedOrders");

        // describedOrders: String = "my orders described"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "describedOrders");

        // hideOrders(): boolean = true
        validator.assertAnyFailuresContaining(clsIdUnderTest, "hideOrders");

        // disableOrders(): String = "my orders disabled"
        validator.assertAnyFailuresContaining(clsIdUnderTest, "disableOrders");

    }

    // -- INCUBATING

    @Test @Disabled("this case has no vaildation refiner yet")
    void invalidPropertyAnnotationOnAction_shouldFail() {
        validator.assertAnyFailuresContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidPropertyAnnotationOnAction.class)),
                "TODO");
    }

//    @Test
//    void orphanedActionSupportNotEnforced_shouldFail() {
//
//        val validateDomainModel = new DomainModelValidator();
//
//        assertThrows(DomainModelException.class, validateDomainModel::run);
//        assertTrue(validateDomainModel.anyMatchesContaining(
//                OrphanedPrefixedAction.class,
//                "is assumed to support"));
//    }

    // -- HELPER

    private String validationMessage(
            final String className,
            final String memberName) {
        return Validation.UNSATISFIED_DOMAIN_INCLUDE_SEMANTICS
                .getMessageForTypeAndMemberId(className, memberName);
    }

}
