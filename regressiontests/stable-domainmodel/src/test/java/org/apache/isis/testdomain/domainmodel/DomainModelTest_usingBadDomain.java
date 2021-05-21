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
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.bad.AmbiguousMixinAnnotations;
import org.apache.isis.testdomain.model.bad.AmbiguousTitle;
import org.apache.isis.testdomain.model.bad.Configuration_usingInvalidDomain;
import org.apache.isis.testdomain.model.bad.InvalidActionOverloading;
import org.apache.isis.testdomain.model.bad.InvalidLogicalTypeNameClash;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedActionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedCollectionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedPropertySupport;
import org.apache.isis.testdomain.model.bad.InvalidPropertyAnnotationOnAction;
import org.apache.isis.testing.integtestsupport.applib.validate.DomainModelValidator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(AmbiguousTitle.class)),
                "conflict for determining a strategy for retrieval of title"));
    }

    @Test
    void orphanedActionSupport_shouldFail() {
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedActionSupport.class)),
                "InvalidOrphanedActionSupport#hideOrphaned: has annotation @MemberSupport, "
                + "is assumed to support"));

        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedActionSupport.class)),
                "InvalidOrphanedActionSupport#hideMe: "
                + "is assumed to support a property, collection or action. "
                + "Unmet constraint(s): unsupported method signature or orphaned "
                + "(not associated with a member)"));
    }


    @Test
    void orphanedPropertySupport_shouldFail() {
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedPropertySupport.class)),
                "InvalidOrphanedPropertySupport#hideMyProperty: has annotation @MemberSupport, "
                + "is assumed to support"));

        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedPropertySupport.class)),
                "InvalidOrphanedPropertySupport#hideMe: "
                + "is assumed to support a property, collection or action. "
                + "Unmet constraint(s): unsupported method signature or orphaned "
                + "(not associated with a member)"));
    }

    @Test
    void orphanedCollectionSupport_shouldFail() {
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedCollectionSupport.class)),
                "InvalidOrphanedCollectionSupport#hideMyCollection: has annotation @MemberSupport, "
                + "is assumed to support"));

        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidOrphanedCollectionSupport.class)),
                "InvalidOrphanedCollectionSupport#hideMe: "
                + "is assumed to support a property, collection or action. "
                + "Unmet constraint(s): unsupported method signature or orphaned "
                + "(not associated with a member)"));
    }

    @Test
    void actionOverloading_shouldFail() {
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidActionOverloading.class)),
                "Action method overloading is not allowed"));
    }

    @Test
    void logicalTypeNameClash_shouldFail() {
        assertTrue(
            validator.anyMatchesContaining(
                    Identifier.classIdentifier(LogicalType.fqcn(InvalidLogicalTypeNameClash.VariantA.class)),
                    "Logical-type-name (aka. object-type) 'isis.testdomain.InvalidLogicalTypeNameClash' "
                    + "mapped to multiple classes")
            || validator.anyMatchesContaining(
                    Identifier.classIdentifier(LogicalType.fqcn(InvalidLogicalTypeNameClash.VariantB.class)),
                    "Logical-type-name (aka. object-type) 'isis.testdomain.InvalidLogicalTypeNameClash' "
                    + "mapped to multiple classes"));
    }


    @ParameterizedTest
    @MethodSource("provideAmbiguousMixins")
    void ambiguousMixinAnnotions_shouldFailValidation(
            final Class<?> mixinClass,
            final Class<? extends Annotation> annotationType,
            final String mixinMethodName) {

        final String annotationLiteral = "@" + annotationType.getSimpleName();
        assertTrue(validator.anyMatchesContaining(
                Identifier.propertyOrCollectionIdentifier(LogicalType.fqcn(mixinClass), mixinMethodName),
                String.format("Annotation %s on both method and type level is not allowed", annotationLiteral)));
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

    // -- INCUBATING

    @Test @Disabled("this case has no vaildation refiner yet")
    void invalidPropertyAnnotationOnAction_shouldFail() {
        assertTrue(validator.anyMatchesContaining(
                Identifier.classIdentifier(LogicalType.fqcn(InvalidPropertyAnnotationOnAction.class)),
                "TODO"));
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
}
