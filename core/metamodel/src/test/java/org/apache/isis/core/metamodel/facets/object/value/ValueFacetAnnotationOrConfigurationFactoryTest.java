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
package org.apache.isis.core.metamodel.facets.object.value;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetAnnotation;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetAnnotationOrConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderUtil;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;

import lombok.val;

public class ValueFacetAnnotationOrConfigurationFactoryTest extends AbstractFacetFactoryTest {

    private ValueFacetAnnotationOrConfigurationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ValueFacetAnnotationOrConfigurationFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyParseableUsingParserName2")
    public static class MyParseableUsingParserName2 extends AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderName> {

        /**
         * Required since is a Parser.
         */
        public MyParseableUsingParserName2() {
        }

    }

    public void testFacetPickedUp() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

        final ValueFacet facet = facetedMethod.getFacet(ValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ValueFacetAnnotation);
    }

    public void testFacetFacetHolderStored() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

        final ValueFacetAnnotation valueFacet = (ValueFacetAnnotation) facetedMethod.getFacet(ValueFacet.class);
        assertEquals(facetedMethod, valueFacet.getFacetHolder());
    }

    public void testNoMethodsRemoved() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderUsingSemanticsProviderName")
    public static class MyValueSemanticsProviderUsingSemanticsProviderName extends AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderName> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderUsingSemanticsProviderName() {
        }
    }

    public void testPickUpSemanticsProviderViaNameAndInstallsValueFacet() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderUsingSemanticsProviderName.class, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(ValueFacet.class));
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderUsingSemanticsProviderClass.class)
    public static class MyValueSemanticsProviderUsingSemanticsProviderClass extends AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderClass> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderUsingSemanticsProviderClass() {
        }
    }

    public void testPickUpSemanticsProviderViaClassAndInstallsValueFacet() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(ValueFacet.class));
    }

    public void testValueSemanticsProviderMustBeAValueSemanticsProvider() {
        // no test, because compiler prevents us from nominating a class that
        // doesn't
        // implement ValueSemanticsProvider
    }

    public void testValueSemanticsProviderThatIsNotAParserDoesNotInstallParseableFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));
        assertNull(facetedMethod.getFacet(ParseableFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatIsAParser")
    public static class MyValueSemanticsProviderThatIsAParser extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsAParser> implements Parser<MyValueSemanticsProviderThatIsAParser> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatIsAParser() {
        }

        @Override
        public MyValueSemanticsProviderThatIsAParser parseTextEntry(
                final Object context,
                final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueSemanticsProviderThatIsAParser object) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueSemanticsProviderThatIsAParser object, final String usingMask) {
            return null;
        }

        @Override
        public String parseableTitleOf(final MyValueSemanticsProviderThatIsAParser existing) {
            return null;
        }

        @Override
        public int typicalLength() {
            return 0;
        }
    }

    public void testValueSemanticsProviderThatIsAParserInstallsParseableFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(ParseableFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTitleFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(TitleFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTypicalLengthFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(TypicalLengthFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotADefaultsProviderDoesNotInstallDefaultedFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));
        assertNull(facetedMethod.getFacet(DefaultedFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatIsADefaultsProvider")
    public static class MyValueSemanticsProviderThatIsADefaultsProvider extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsADefaultsProvider> implements DefaultsProvider<MyValueSemanticsProviderThatIsADefaultsProvider> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatIsADefaultsProvider() {
        }

        @Override
        public MyValueSemanticsProviderThatIsADefaultsProvider getDefaultValue() {
            return new MyValueSemanticsProviderThatIsADefaultsProvider();
        }
    }

    public void testValueSemanticsProviderThatIsADefaultsProviderInstallsDefaultedFacet() {
        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatIsADefaultsProvider.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(DefaultedFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotAnEncoderDecoderDoesNotInstallEncodeableFacet() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(EncodableFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatIsAnEncoderDecoder")
    public static class MyValueSemanticsProviderThatIsAnEncoderDecoder extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsAnEncoderDecoder> implements EncoderDecoder<MyValueSemanticsProviderThatIsAnEncoderDecoder> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatIsAnEncoderDecoder() {
        }

        @Override
        public MyValueSemanticsProviderThatIsAnEncoderDecoder fromEncodedString(final String encodedString) {
            return null;
        }

        @Override
        public String toEncodedString(final MyValueSemanticsProviderThatIsAnEncoderDecoder toEncode) {
            return null;
        }
    }

    public void testValueSemanticsProviderThatIsAnEncoderInstallsEncodeableFacet() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatIsAnEncoderDecoder.class, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(EncodableFacet.class));
    }

    public void testImmutableFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberImmutableDefault {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(MyNumberImmutableDefault.class, methodRemover, facetedMethod));

        final ImmutableFacet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesImmutableSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesImmutableSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesImmutableSemantic> {

    }

    public void testImmutableFacetsIsInstalledIfSpecifiesImmutable() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatSpecifiesImmutableSemantic.class, methodRemover, facetedMethod));

        final ImmutableFacet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    public void testEqualByContentFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberEqualByContentDefault {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(MyNumberEqualByContentDefault.class, methodRemover, facetedMethod));

        final EqualByContentFacet facet = facetedMethod.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic> {

    }

    public void testEqualByContentFacetsIsInstalledIfSpecifiesEqualByContent() {

        facetFactory.process(ProcessClassContext
                .forTesting(MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic.class, methodRemover, facetedMethod));

        final EqualByContentFacet facet = facetedMethod.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value()
    public static class MyValueWithSemanticsProviderSpecifiedUsingConfiguration extends AbstractValueSemanticsProvider<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> implements Parser<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> {

        /**
         * Required since is a SemanticsProvider.
         */
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration parseTextEntry(
                final Object context,
                final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueWithSemanticsProviderSpecifiedUsingConfiguration object) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueWithSemanticsProviderSpecifiedUsingConfiguration object, final String usingMask) {
            return null;
        }

        @Override
        public String parseableTitleOf(final MyValueWithSemanticsProviderSpecifiedUsingConfiguration existing) {
            return null;
        }

        @Override
        public int typicalLength() {
            return 0;
        }
    }

    public void testSemanticsProviderNameCanBePickedUpFromConfiguration() {

        // given
        val className = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueWithSemanticsProviderSpecifiedUsingConfiguration";
        val configKey = ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX;

        // when
        metaModelContext
        .runWithConfigProperties(
            map->map.put(configKey, className),
            ()->{
                facetFactory.process(ProcessClassContext.forTesting(MyValueWithSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));
            });

        // then
        final ValueFacetAbstract facet = (ValueFacetAbstract) facetedMethod.getFacet(ValueFacet.class);
        assertNotNull(facet);
        // should also be a ParserFacet, since the VSP implements Parser
        final ParseableFacet parseableFacet = facetedMethod.getFacet(ParseableFacet.class);
        assertNotNull(parseableFacet);
    }

    public static class NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration extends AbstractValueSemanticsProvider<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> implements Parser<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> {
        /**
         * Required since is a SemanticsProvider.
         */
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration parseTextEntry(
                final Object context,
                final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration object) {
            return null;
        }

        @Override
        public String displayTitleOf(final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration object, final String usingMask) {
            return null;
        }

        @Override
        public String parseableTitleOf(final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration existing) {
            return null;
        }

        @Override
        public int typicalLength() {
            return 0;
        }
    }

    public void testNonAnnotatedValueCanPickUpSemanticsProviderFromConfiguration() {

        // given
        val className = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration";
        val configKey = ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX;

        // when
        metaModelContext
        .runWithConfigProperties(
            map->map.put(configKey, className),
            ()->{

                facetFactory.process(ProcessClassContext.forTesting(NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));
            });

        // then
        final ValueFacetAbstract facet = (ValueFacetAbstract) facetedMethod.getFacet(ValueFacet.class);
        assertNotNull(facet);
        // should also be a ParserFacet, since the VSP implements Parser
        final ParseableFacet parseableFacet = facetedMethod.getFacet(ParseableFacet.class);
        assertNotNull(parseableFacet);
    }

    private String canonical(final String className) {
        return className.replace('$', '.');
    }

}
