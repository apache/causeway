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

package org.apache.isis.core.progmodel.facets.object.value;

import java.util.List;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.progmodel.facets.object.ebc.EqualByContentFacet;

public class ValueFacetFactoryTest extends AbstractFacetFactoryTest {

    private ValueFacetFactory facetFactory;
    private PropertiesConfiguration propertiesConfiguration;
    private RuntimeContext runtimeContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ValueFacetFactory();
        propertiesConfiguration = new PropertiesConfiguration();
        facetFactory.setIsisConfiguration(propertiesConfiguration);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyParseableUsingParserName2")
    public static class MyParseableUsingParserName2 extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderName> {

        /**
         * Required since is a Parser.
         */
        public MyParseableUsingParserName2() {
        }

    }

    public void testFacetPickedUp() {

        facetFactory.process(MyParseableUsingParserName2.class, methodRemover, facetHolder);

        final ValueFacet facet = facetHolder.getFacet(ValueFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ValueFacetAnnotation);
    }

    public void testFacetFacetHolderStored() {

        facetFactory.process(MyParseableUsingParserName2.class, methodRemover, facetHolder);

        final ValueFacetAnnotation valueFacet = (ValueFacetAnnotation) facetHolder.getFacet(ValueFacet.class);
        assertEquals(facetHolder, valueFacet.getFacetHolder());
    }

    public void testNoMethodsRemoved() {

        facetFactory.process(MyParseableUsingParserName2.class, methodRemover, facetHolder);

        assertNoMethodsRemoved();
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderUsingSemanticsProviderName")
    public static class MyValueSemanticsProviderUsingSemanticsProviderName extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderName> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderUsingSemanticsProviderName() {
        }
    }

    public void testPickUpSemanticsProviderViaNameAndInstallsValueFacet() {

        facetFactory.process(MyValueSemanticsProviderUsingSemanticsProviderName.class, methodRemover, facetHolder);

        assertNotNull(facetHolder.getFacet(ValueFacet.class));
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderUsingSemanticsProviderClass.class)
    public static class MyValueSemanticsProviderUsingSemanticsProviderClass extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderUsingSemanticsProviderClass> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderUsingSemanticsProviderClass() {
        }
    }

    public void testPickUpSemanticsProviderViaClassAndInstallsValueFacet() {

        facetFactory.process(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetHolder);

        assertNotNull(facetHolder.getFacet(ValueFacet.class));
    }

    public void testValueSemanticsProviderMustBeAValueSemanticsProvider() {
        // no test, because compiler prevents us from nominating a class that doesn't
        // implement ValueSemanticsProvider
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderWithoutNoArgConstructor.class)
    public static class MyValueSemanticsProviderWithoutNoArgConstructor extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderWithoutNoArgConstructor> {

        // no no-arg constructor

        // pass in false for an immutable, which isn't the default
        public MyValueSemanticsProviderWithoutNoArgConstructor(final int value) {
            super(false, false);
        }
    }

    public void testValueSemanticsProviderMustHaveANoArgConstructor() {
        facetFactory.process(MyValueSemanticsProviderWithoutNoArgConstructor.class, methodRemover, facetHolder);

        // the fact that we have an immutable means that the provider wasn't picked up
        assertNotNull(facetHolder.getFacet(ImmutableFacet.class));
    }

    @Value(semanticsProviderClass = MyValueSemanticsProviderWithoutPublicNoArgConstructor.class)
    public static class MyValueSemanticsProviderWithoutPublicNoArgConstructor extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderWithoutPublicNoArgConstructor> {

        // no public no-arg constructor

        // pass in false for an immutable, which isn't the default
        MyValueSemanticsProviderWithoutPublicNoArgConstructor() {
            super(false, false);
        }

        public MyValueSemanticsProviderWithoutPublicNoArgConstructor(final int value) {
        }
    }

    public void testValueSemanticsProviderMustHaveAPublicNoArgConstructor() {
        facetFactory.process(MyValueSemanticsProviderWithoutPublicNoArgConstructor.class, methodRemover, facetHolder);

        // the fact that we have an immutable means that the provider wasn't picked up
        assertNotNull(facetHolder.getFacet(ImmutableFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotAParserDoesNotInstallParseableFacet() {
        facetFactory.process(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetHolder);
        assertNull(facetHolder.getFacet(ParseableFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatIsAParser")
    public static class MyValueSemanticsProviderThatIsAParser extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsAParser> implements
        Parser<MyValueSemanticsProviderThatIsAParser> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatIsAParser() {
        }

        @Override
        public MyValueSemanticsProviderThatIsAParser parseTextEntry(
            final Object context, final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueSemanticsProviderThatIsAParser object) {
            return null;
        }

        @Override
        public String displayTitleOf(MyValueSemanticsProviderThatIsAParser object, String usingMask) {
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
        facetFactory.process(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetHolder);
        assertNotNull(facetHolder.getFacet(ParseableFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTitleFacet() {
        facetFactory.process(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetHolder);
        assertNotNull(facetHolder.getFacet(TitleFacet.class));
    }

    public void testValueSemanticsProviderThatIsAParserInstallsTypicalLengthFacet() {
        facetFactory.process(MyValueSemanticsProviderThatIsAParser.class, methodRemover, facetHolder);
        assertNotNull(facetHolder.getFacet(TypicalLengthFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotADefaultsProviderDoesNotInstallDefaultedFacet() {
        facetFactory.process(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetHolder);
        assertNull(facetHolder.getFacet(DefaultedFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatIsADefaultsProvider")
    public static class MyValueSemanticsProviderThatIsADefaultsProvider extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsADefaultsProvider> implements
        DefaultsProvider<MyValueSemanticsProviderThatIsADefaultsProvider> {

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
        facetFactory.process(MyValueSemanticsProviderThatIsADefaultsProvider.class, methodRemover, facetHolder);
        assertNotNull(facetHolder.getFacet(DefaultedFacet.class));
    }

    public void testValueSemanticsProviderThatIsNotAnEncoderDecoderDoesNotInstallEncodeableFacet() {

        facetFactory.process(MyValueSemanticsProviderUsingSemanticsProviderClass.class, methodRemover, facetHolder);

        assertNull(facetHolder.getFacet(EncodableFacet.class));
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatIsAnEncoderDecoder")
    public static class MyValueSemanticsProviderThatIsAnEncoderDecoder extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatIsAnEncoderDecoder> implements
        EncoderDecoder<MyValueSemanticsProviderThatIsAnEncoderDecoder> {

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

        facetFactory.process(MyValueSemanticsProviderThatIsAnEncoderDecoder.class, methodRemover, facetHolder);

        assertNotNull(facetHolder.getFacet(EncodableFacet.class));
    }

    public void testImmutableFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberImmutableDefault {
        }

        facetFactory.process(MyNumberImmutableDefault.class, methodRemover, facetHolder);

        final ImmutableFacet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatSpecifiesImmutableSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesImmutableSemantic extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesImmutableSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesImmutableSemantic() {
            super(true, true);
        }
    }

    public void testImmutableFacetsIsInstalledIfSpecifiesImmutable() {

        facetFactory.process(MyValueSemanticsProviderThatSpecifiesImmutableSemantic.class, methodRemover, facetHolder);

        final ImmutableFacet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic() {
            super(false, true);
        }
    }

    public void testImmutableFacetsIsNotInstalledIfSpecifiesNotImmutable() {

        facetFactory.process(MyValueSemanticsProviderThatSpecifiesNotImmutableSemantic.class, methodRemover,
            facetHolder);

        final ImmutableFacet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNull(facet);
    }

    public void testEqualByContentFacetsIsInstalledIfNoSemanticsProviderSpecified() {

        @Value()
        class MyNumberEqualByContentDefault {
        }

        facetFactory.process(MyNumberEqualByContentDefault.class, methodRemover, facetHolder);

        final EqualByContentFacet facet = facetHolder.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic() {
            super(true, true);
        }
    }

    public void testEqualByContentFacetsIsInstalledIfSpecifiesEqualByContent() {

        facetFactory.process(MyValueSemanticsProviderThatSpecifiesEqualByContentSemantic.class, methodRemover,
            facetHolder);

        final EqualByContentFacet facet = facetHolder.getFacet(EqualByContentFacet.class);
        assertNotNull(facet);
    }

    @Value(semanticsProviderName = "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic")
    public static class MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic extends
        AbstractValueSemanticsProvider<MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic> {

        /**
         * Required since is a ValueSemanticsProvider.
         */
        public MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic() {
            super(false, false);
        }
    }

    public void testEqualByContentFacetsIsNotInstalledIfSpecifiesNotEqualByContent() {

        facetFactory.process(MyValueSemanticsProviderThatSpecifiesNotEqualByContentSemantic.class, methodRemover,
            facetHolder);

        final EqualByContentFacet facet = facetHolder.getFacet(EqualByContentFacet.class);
        assertNull(facet);
    }

    @Value()
    public static class MyValueWithSemanticsProviderSpecifiedUsingConfiguration extends
        AbstractValueSemanticsProvider<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> implements
        Parser<MyValueWithSemanticsProviderSpecifiedUsingConfiguration> {

        /**
         * Required since is a SemanticsProvider.
         */
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public MyValueWithSemanticsProviderSpecifiedUsingConfiguration parseTextEntry(
            final Object context, final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final MyValueWithSemanticsProviderSpecifiedUsingConfiguration object) {
            return null;
        }

        @Override
        public String displayTitleOf(MyValueWithSemanticsProviderSpecifiedUsingConfiguration object, String usingMask) {
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
        final String className =
            "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$MyValueWithSemanticsProviderSpecifiedUsingConfiguration";
        propertiesConfiguration.add(ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX
            + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX, className);
        facetFactory.process(MyValueWithSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover, facetHolder);
        final ValueFacetAbstract facet = (ValueFacetAbstract) facetHolder.getFacet(ValueFacet.class);
        assertNotNull(facet);
        // should also be a ParserFacet, since the VSP implements Parser
        final ParseableFacet parseableFacet = facetHolder.getFacet(ParseableFacet.class);
        assertNotNull(parseableFacet);
    }

    public static class NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration extends
        AbstractValueSemanticsProvider<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> implements
        Parser<NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration> {
        /**
         * Required since is a SemanticsProvider.
         */
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration() {
        }

        @Override
        public NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration parseTextEntry(
            final Object context, final String entry) {
            return null;
        }

        @Override
        public String displayTitleOf(final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration object) {
            return null;
        }

        @Override
        public String displayTitleOf(final NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration object,
            String usingMask) {
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
        final String className =
            "org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactoryTest$NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration";
        propertiesConfiguration.add(ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_PREFIX
            + canonical(className) + ValueSemanticsProviderUtil.SEMANTICS_PROVIDER_NAME_KEY_SUFFIX, className);
        facetFactory.process(NonAnnotatedValueSemanticsProviderSpecifiedUsingConfiguration.class, methodRemover,
            facetHolder);
        final ValueFacetAbstract facet = (ValueFacetAbstract) facetHolder.getFacet(ValueFacet.class);
        assertNotNull(facet);
        // should also be a ParserFacet, since the VSP implements Parser
        final ParseableFacet parseableFacet = facetHolder.getFacet(ParseableFacet.class);
        assertNotNull(parseableFacet);
    }

    private String canonical(final String className) {
        return className.replace('$', '.');
    }

}
