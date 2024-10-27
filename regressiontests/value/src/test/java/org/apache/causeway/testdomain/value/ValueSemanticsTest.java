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
package org.apache.causeway.testdomain.value;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.valuetypes.Configuration_usingValueTypes;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExampleService;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExampleService.Scenario;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValueTypes.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.value-types.big-decimal.display.use-grouping-separator=false"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
@TestInstance(Lifecycle.PER_CLASS)
class ValueSemanticsTest {

    /* debug
    static class TestEnvironment {

        private Locale systemLocale;
        private TimeZone systemTimeZone;

        TestEnvironment() {
            systemLocale = Locale.getDefault();
            systemTimeZone = TimeZone.getDefault();

            if(!Objects.equals(systemLocale, Locale.US)) {
                System.err.println("DEBUG: setting test Locale to US");
                //log.warn("setting test Locale to US");
                Locale.setDefault(Locale.US);
            }

            if(!Objects.equals(systemTimeZone, TimeZone.getTimeZone("GMT"))) {
                System.err.println("DEBUG: setting test TimeZone to GMT");
                //log.warn("setting test TimeZone to GMT");
                TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            }
        }

        void cleanup() {
            Locale.setDefault(systemLocale);
            TimeZone.setDefault(systemTimeZone);
        }

    }
     */

    @Test
    void fullTypeCoverage() {

        //debug
//        valueSemanticsResolver.streamClassesWithValueSemantics()
//            .forEach(valueType->System.err.printf("%s%n", valueType.getName()));

        final Set<Class<?>> valueTypesCovered = valueTypeExampleService.streamExamples()
        .map(ValueTypeExample::getValueType)
        .collect(Collectors.toSet());

        final Set<Class<?>> valueTypesKnown = valueSemanticsResolver.streamClassesWithValueSemantics()
        .collect(Collectors.toSet());

        //TODO[CAUSEWAY-2877] yet excluded from coverage ...
        valueTypesKnown.remove(TreeNode.class);
        valueTypesKnown.remove(Markdown.class);
        valueTypesKnown.remove(AsciiDoc.class);

        var valueTypesNotCovered = _Sets.minus(valueTypesKnown, valueTypesCovered);

        assertTrue(valueTypesNotCovered.isEmpty(), ()->
            String.format("value-types not covered by tests:\n\t%s",
                    valueTypesNotCovered.stream()
                    .map(Class::getName)
                    .collect(Collectors.joining("\n\t"))));
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideValueTypeExamples")
    <T> void actionInteraction(
            final String name,
            final Class<T> valueType,
            final ValueTypeExample<T> example) {

        //var env = new TestEnvironment();

        assertNotNull(example);

        var tester = createTester(example);
        var actionInteractionProbe = serviceInjector.injectServicesInto(
                new ActionInteractionProbeImpl<>(name, valueType, example, tester));

        tester.actionInteraction("sampleAction",
                _Utils.interactionContext(),
                example::getUpdateValue,
                actionInteractionProbe);

       // env.cleanup();
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideValueTypeExamples")
    <T> void propertyInteraction(
            final String name,
            final Class<T> valueType,
            final ValueTypeExample<T> example) {

        //var env = new TestEnvironment();

        assertNotNull(example);

        var tester = createTester(example);
        var propertyInteractionProbe = serviceInjector.injectServicesInto(
                new PropertyInteractionProbeImpl<>(name, valueType, example, tester));

        tester.propertyInteraction("value",
                _Utils.interactionContext(),
                managedProp->example.getUpdateValue(),
                propertyInteractionProbe);

       // env.cleanup();
    }

    // -- HELPER

    private <T> ValueSemanticsTester<T> createTester(final ValueTypeExample<T> example) {
        return serviceInjector.injectServicesInto(
                new ValueSemanticsTester<T>(example.getValueType(), example));
    }

    // -- DEPENDENCIES

    @Inject ValueTypeExampleService valueTypeExampleService;
    @Inject SpecificationLoader specLoader;
    @Inject InteractionService interactionService;
    @Inject ServiceInjector serviceInjector;
    @Inject ValueSemanticsResolver valueSemanticsResolver;
    @Inject SchemaValueMarshaller valueMarshaller;
    @Inject WrapperFactory wrapperFactory;

    Stream<Arguments> provideValueTypeExamples() {
        return valueTypeExampleService.streamScenarios()
                .map(Scenario::getArguments);
    }

}
