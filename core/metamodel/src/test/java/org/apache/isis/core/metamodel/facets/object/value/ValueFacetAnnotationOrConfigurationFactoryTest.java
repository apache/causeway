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
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.config.internal._Config;
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
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class ValueFacetAnnotationOrConfigurationFactoryTest extends AbstractFacetFactoryTest {

    private JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private ValueFacetAnnotationOrConfigurationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ValueFacetAnnotationOrConfigurationFactory();
        facetFactory.setServicesInjector(stubServicesInjector);
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

        facetFactory.process(new ProcessClassContext(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

        final ValueFacet facet = facetedMethod.getFacet(ValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ValueFacetAnnotation);
    }

    public void testFacetFacetHolderStored() {

        facetFactory.process(new ProcessClassContext(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

        final ValueFacetAnnotation valueFacet = (ValueFacetAnnotation) facetedMethod.getFacet(ValueFacet.class);
        assertEquals(facetedMethod, valueFacet.getFacetHolder());
    }

    public void testNoMethodsRemoved() {

        facetFactory.process(new ProcessClassContext(MyParseableUsingParserName2.class, methodRemover, facetedMethod));

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

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderUsingSemanticsProviderName.class, methodRemover, facetedMethod));

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

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(ValueFacet.class));
    }

    public void testValueSemanticsProviderMustBeAValueSemanticsProvider() {
        // no test, because compiler prevents us from nominating a class that
        // doesn't
        // implement ValueSemanticsProvider
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderWithoutNoArgConstructor.class)
    public static class MyValueSemanticsProviderWithoutNoArgConstructor extends AbstractValueSemanticsProvider<MyValueSemanticsProviderWithoutNoArgConstructor> {

        // no no-arg constructor

        // pass in false for an immutable, which isn't the default
        public MyValueSemanticsProviderWithoutNoArgConstructor(final int value) {
            super(false, false);
        }
    }

    public void testValueSemanticsProviderMustHaveANoArgConstructor() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderWithoutNoArgConstructor.class, methodRemover, facetedMethod));

        // the fact that we have an immutable means that the provider wasn't
        // picked up
        assertNotNull(facetedMethod.getFacet(ImmutableFacet.class));
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderWithoutPublicNoArgConstructor.class)
    public static class MyValueSemanticsProviderWithoutPublicNoArgConstructor extends AbstractValueSemanticsProvider<MyValueSemanticsProviderWithoutPublicNoArgConstructor> {

        // no public no-arg constructor

        // pass in false for an immutable, which isn't the default
        MyValueSemanticsProviderWithoutPublicNoArgConstructor() {
            super(false, false);
        }

        public MyValueSemanticsProviderWithoutPublicNoArgConstructor(final int value) {
        }
    }

    public void testValueSemanticsProviderMustHaveAPublicNoArgConstructor() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderWithoutPublicNoArgConstructor.class, methodRemover, facetedMethod));

        // the fact that we have an immutable means that the provider wasn't
        // picked up
        assertNotNull(facetedMethod.getFacet(ImmutableFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotAParserDoesNotInstallParseableFacet() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));
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
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(ParseableFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTitleFacet() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(TitleFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTypicalLengthFacet() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(TypicalLengthFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotADefaultsProviderDoesNotInstallDefaultedFacet() {
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));
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
        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatIsADefaultsProvider.class, methodRemover, facetedMethod));
        assertNotNull(facetedMethod.getFacet(DefaultedFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotAnEncoderDecoderDoesNotInstallEncodeableFacet() {

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetedMethod));

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

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatIsAnEncoderDecoder.class, methodRemover, facetedMethod));

        assertNotNull(facetedMethod.getFacet(EncodableFacet.class));
    }

    public void testImmutableFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberImmutableDefault {
        }

        facetFactory.process(new ProcessClassContext(MyNumberImmutableDefault.class, methodRemover, facetedMethod));

        final ImmutableFacet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesImmutableSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesImmutableSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesImmutableSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesImmutableSemantic() {
            super(true, true);
        }
    }

    public void testImmutableFacetsIsInstalledIfSpecifiesImmutable() {

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatSpecifiesImmutableSemantic.class, methodRemover, facetedMethod));

        final ImmutableFacet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic() {
            super(false, true);
        }
    }

    public void testImmutableFacetsIsNotInstalledIfSpecifiesNotImmutable() {

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic.class, methodRemover, facetedMethod));

        final ImmutableFacet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNull(facet);
    }

    public void testEqualByContentFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberEqualByContentDefault {
        }

        facetFactory.process(new ProcessClassContext(MyNumberEqualByContentDefault.class, methodRemover, facetedMethod));

        final EqualByContentFacet facet = facetedMethod.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic() {
            super(true, true);
        }
    }

    public void testEqualByContentFacetsIsInstalledIfSpecifiesEqualByContent() {

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic.class, methodRemover, facetedMethod));

        final EqualByContentFacet facet = facetedMethod.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic extends AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic() {
            super(false, false);
        }
    }

    public void testEqualByContentFacetsIsNotInstalledIfSpecifiesNotEqualByContent() {

        facetFactory.process(new ProcessClassContext(MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic.class, methodRemover, facetedMethod));

        final EqualByContentFacet facet = facetedMethod.getFacet(EqualByContentFacet.class);
        assertNull(facet);
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
        final String className = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$MyValueWithSemanticsProviderSpecifiedUsingConfiguration";

        _Config.clear();
        _Config.put(ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX, className);

        // when
        facetFactory.process(new ProcessClassContext(MyValueWithSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));

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
        final String className = "org.apache.isis.core.metamodel.facets.object.value.ValueFacetAnnotationOrConfigurationFactoryTest$NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration";
        _Config.clear();
        _Config.put(ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX, className);

        // when
        facetFactory.process(new ProcessClassContext(NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover, facetedMethod));


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
