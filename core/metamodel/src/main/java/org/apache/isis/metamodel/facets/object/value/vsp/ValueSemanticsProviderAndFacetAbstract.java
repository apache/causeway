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

package org.apache.isis.metamodel.facets.object.value.vsp;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.commons.exceptions.UnknownTypeException;
import org.apache.isis.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.commons.LocaleUtil;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public abstract class ValueSemanticsProviderAndFacetAbstract<T> extends FacetAbstract implements ValueSemanticsProvider<T>, EncoderDecoder<T>, Parser<T>, DefaultsProvider<T> {

    private final Class<T> adaptedClass;
    private final int typicalLength;
    private final int maxLength;
    private final boolean immutable;
    private final boolean equalByContent;
    private final T defaultValue;

    public enum Immutability {
        IMMUTABLE,
        NOT_IMMUTABLE;

        public static Immutability of(boolean immutable) {
            return immutable? IMMUTABLE: NOT_IMMUTABLE;
        }
    }

    public enum EqualByContent {
        HONOURED,
        NOT_HONOURED;

        public static EqualByContent of(boolean equalByContent) {
            return equalByContent? HONOURED: NOT_HONOURED;
        }
    }

    /**
     * Lazily looked up per {@link #getSpecification()}.
     */
    private ObjectSpecification specification;

    public ValueSemanticsProviderAndFacetAbstract(
            final Class<? extends Facet> adapterFacetType,
            final FacetHolder holder,
            final Class<T> adaptedClass,
            final int typicalLength,
            final int maxLength,
            final Immutability immutability,
            final EqualByContent equalByContent,
            final T defaultValue) {

        super(adapterFacetType, holder, Derivation.NOT_DERIVED);
        this.adaptedClass = adaptedClass;
        this.typicalLength = typicalLength;
        this.maxLength = maxLength;
        this.immutable = (immutability == Immutability.IMMUTABLE);
        this.equalByContent = (equalByContent == EqualByContent.HONOURED);
        this.defaultValue = defaultValue;
    }

    public ObjectSpecification getSpecification() {
        if (specification == null) {
            specification = getSpecificationLoader().loadSpecification(getAdaptedClass());
        }
        return specification;
    }

    /**
     * The underlying class that has been adapted.
     *
     * <p>
     * Used to determine whether an empty string can be parsed, (for primitive
     * types a non-null entry is required, see {@link #mustHaveEntry()}), and
     * potentially useful for debugging.
     */
    public final Class<T> getAdaptedClass() {
        return adaptedClass;
    }

    /**
     * We don't replace any (none no-op) facets.
     *
     * <p>
     * For example, if there is already a {@link PropertyDefaultFacet} then we
     * shouldn't replace it.
     */
    @Override
    public boolean alwaysReplace() {
        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ValueSemanticsProvider implementation
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public EncoderDecoder<T> getEncoderDecoder() {
        return this;
    }

    @Override
    public Parser<T> getParser() {
        return this;
    }

    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return this;
    }

    @Override
    public boolean isEqualByContent() {
        return equalByContent;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Parser implementation
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public T parseTextEntry(final Object context, final String entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        if (entry.trim().equals("")) {
            if (mustHaveEntry()) {
                throw new InvalidEntryException("An entry is required");
            } else {
                return null;
            }
        }
        return doParse(context, entry);
    }

    /**
     * @param context
     *            - the underlying object, or <tt>null</tt>.
     * @param entry
     *            - the proposed new object, as a string representation to be
     *            parsed
     */
    protected T doParse(Object context, String entry) {
        return doParse(entry, context);
    }

    // REVIEW: this method used to take Localization as a third param, could now inline
    protected T doParse(String entry, Object context) {
        return doParse(context, entry);
    }

    /**
     * Whether a non-null entry is required, used by parsing.
     *
     * <p>
     * Adapters for primitives will return <tt>true</tt>.
     */
    private final boolean mustHaveEntry() {
        return adaptedClass.isPrimitive();
    }

    @Override
    public String displayTitleOf(final Object object) {
        if (object == null) {
            return "";
        }
        return titleString(object);
    }

    @Override
    public String displayTitleOf(final Object object, final String usingMask) {
        if (object == null) {
            return "";
        }
        return titleStringWithMask(object, usingMask);
    }

    /**
     * Defaults to {@link Parser#displayTitleOf(Object)}.
     */
    @Override
    public String parseableTitleOf(final Object existing) {
        return displayTitleOf(existing);
    }

    protected String titleString(final Format formatter, final Object object) {
        return object == null ? "" : formatter.format(object);
    }

    /**
     * Return a string representation of aforesaid object.
     */
    protected abstract String titleString(Object object);

    public abstract String titleStringWithMask(final Object value, final String usingMask);

    @Override
    public final int typicalLength() {
        return this.typicalLength;
    }

    @Override
    public final int maxLength() {
        return this.maxLength;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DefaultsProvider implementation
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public T getDefaultValue() {
        return this.defaultValue;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EncoderDecoder implementation
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final Object object) {
        return doEncode(object);
    }

    @Override
    public T fromEncodedString(final String data) {
        return doRestore(data);
    }

    /**
     * Hook method to perform the actual encoding.
     */
    protected abstract String doEncode(Object object);

    /**
     * Hook method to perform the actual restoring.
     */
    protected abstract T doRestore(String data);

    // ///////////////////////////////////////////////////////////////////////////
    // Helper: Locale handling
    // ///////////////////////////////////////////////////////////////////////////

    protected NumberFormat determineNumberFormat(FormatIdentifier formatIdentifier) {
        final String formatRequired = getConfiguration()
                .getValue().getFormatOrElse(formatIdentifier, null);  
                
        if (formatRequired != null) {
            return new DecimalFormat(formatRequired);
        } else {
            return NumberFormat.getNumberInstance(findLocale());
        }
    }

    private Locale findLocale() {
        final String localeStr = getConfiguration().getLocale();
        final Locale findLocale = LocaleUtil.findLocale(localeStr);
        return findLocale != null ? findLocale : Locale.getDefault();
    }

    // //////////////////////////////////////////////////////////
    // Helper: createAdapter
    // //////////////////////////////////////////////////////////

    protected ObjectAdapter createAdapter(final Class<?> type, final Object object) {
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(type);
        if (specification.isNotCollection()) {
            return getObjectAdapterProvider().adapterFor(object);
        } else {
            throw new UnknownTypeException("not an object, is this a collection?");
        }
    }

    // //////////////////////////////////////////////////////////
    // Dependencies (from singleton)
    // //////////////////////////////////////////////////////////

    protected static Clock getClock() {
        return Clock.getInstance();
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("adaptedClass", adaptedClass);
        attributeMap.put("typicalLength", this.typicalLength);
        attributeMap.put("maxLength", this.maxLength);
        attributeMap.put("immutable", this.immutable);
        attributeMap.put("equalByContent", this.equalByContent);
        attributeMap.put("defaultValue", this.defaultValue);
    }

}
