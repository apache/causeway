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
package org.apache.isis.core.metamodel.facets.object.value.vsp;

import java.text.Format;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.exceptions.recoverable.InvalidEntryException;
import org.apache.isis.applib.exceptions.unrecoverable.UnknownTypeException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public abstract class ValueSemanticsProviderAndFacetAbstract<T>
extends FacetAbstract
implements ValueSemanticsProvider<T>, EncoderDecoder<T>, Parser<T>, DefaultsProvider<T> {

    private final Class<T> adaptedClass;
    private final int typicalLength;
    private final int maxLength;
    private final boolean immutable;
    private final boolean equalByContent;
    private final T defaultValue;

    public enum Immutability {
        IMMUTABLE,
        NOT_IMMUTABLE;

        public static Immutability of(final boolean immutable) {
            return immutable? IMMUTABLE: NOT_IMMUTABLE;
        }
    }

    public enum EqualByContent {
        HONOURED,
        NOT_HONOURED;

        public static EqualByContent of(final boolean equalByContent) {
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

        /*
         * precedence = LATE
         * We don't replace any (none no-op) facets.
         *
         * For example, if there is already a {@link PropertyDefaultFacet} then we
         * shouldn't replace it.
         */
        super(adapterFacetType, holder, Facet.Precedence.LOW);
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

    // ///////////////////////////////////////////////////////////////////////////
    // Parser implementation
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public T parseTextRepresentation(final Parser.Context context, final String entry) {
        if (entry == null) {
            throw new IllegalArgumentException();
        }
        if (entry.trim().isEmpty()) {
            if (mustHaveEntry()) {
                throw new InvalidEntryException("An entry is required");
            } else {
                return null;
            }
        }
        return doParse(context, entry);
    }

    public Optional<Exception> tryParseTextEntry(final Parser.Context context, final String entry) {
        try {
            parseTextRepresentation(context, entry);
        } catch (Exception e) {
            return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * @param context
     *            - the underlying object, or <tt>null</tt>.
     * @param entry
     *            - the proposed new object, as a string representation to be
     *            parsed
     */
    protected abstract T doParse(final Parser.Context context, final String entry);


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
    public String presentationValue(final Parser.Context context, final Object object) {
        if (object == null) {
            return "";
        }
        return titleString(object);
    }

    /**
     * Defaults to {@link Parser#presentationValue(org.apache.isis.applib.adapters.Parser.Context, Object)}.
     */
    @Override
    public String parseableTextRepresentation(final Parser.Context context, final Object existing) {
        return presentationValue(context, existing);
    }

    protected String titleString(final Format formatter, final Object object) {
        return object == null ? "" : formatter.format(object);
    }

    /**
     * Return a string representation of aforesaid object.
     */
    protected abstract String titleString(Object object);

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

    // //////////////////////////////////////////////////////////
    // Helper: createAdapter
    // //////////////////////////////////////////////////////////

    protected ManagedObject createAdapter(final Class<?> type, final Object object) {
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(type);
        if (specification.isNotCollection()) {
            return getObjectManager().adapt(object);
        } else {
            throw new UnknownTypeException("not an object, is this a collection?");
        }
    }


    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("adaptedClass", adaptedClass);
        visitor.accept("typicalLength", this.typicalLength);
        visitor.accept("maxLength", this.maxLength);
        visitor.accept("immutable", this.immutable);
        visitor.accept("equalByContent", this.equalByContent);
        visitor.accept("defaultValue", this.defaultValue);
    }


}
