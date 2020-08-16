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
package org.apache.isis.core.metamodel.facets.value.temporal;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Common base for {@link java.time.temporal.Temporal} types.
 * 
 * @since 2.0
 *
 * @param <T> implementing {@link java.time.temporal.Temporal} type
 */
@Log4j2
public abstract class TemporalValueSemanticsProviderAbstract<T extends Temporal> 
extends ValueSemanticsProviderAndFacetAbstract<T>
implements TemporalValueFacet<T> {
    
    @Getter(onMethod = @__(@Override)) protected final TemporalCharacteristic temporalCharacteristic;
    @Getter(onMethod = @__(@Override)) protected final OffsetCharacteristic offsetCharacteristic;
    
    @Getter private DateTimeFormatter encodingFormatter;
    @Getter @Setter private DateTimeFormatter titleFormatter;
    
    /**
     * Keys represent the values which can be configured, 
     * and which are used for the rendering of dates.
     */
    protected final Map<String, DateTimeFormatter> namedFormatters;
    protected Can<Function<String, T>> parsers;
    
    protected final TemporalQuery<T> query;
    protected final BiFunction<TemporalAdjust, T, T> adjuster;
    
    public TemporalValueSemanticsProviderAbstract(
            Class<? extends Facet> adapterFacetType,
            TemporalCharacteristic temporalCharacteristic,
            OffsetCharacteristic offsetCharacteristic,
            FacetHolder holder,
            Class<T> valueType, 
            int typicalLength, 
            int maxLength,
            TemporalQuery<T> query,
            BiFunction<TemporalAdjust, T, T> adjuster) {
        
        super(adapterFacetType, holder, valueType, typicalLength, maxLength, 
                Immutability.IMMUTABLE, EqualByContent.HONOURED, /*DEFAULT_VALUE*/ null);
        
        this.temporalCharacteristic = temporalCharacteristic;
        this.offsetCharacteristic = offsetCharacteristic;
        
        this.query = query;
        this.adjuster = adjuster;
        
        namedFormatters = _Maps.newLinkedHashMap();
        namedFormatters.put("internal_encoding", this.getEncodingFormatter());
        updateParsers();
    }
    
    @Override
    public void appendAttributesTo(Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("temporalCharacteristic", getTemporalCharacteristic());
        attributeMap.put("offsetCharacteristic", getOffsetCharacteristic());
    }
    
    protected void setEncodingFormatter(DateTimeFormatter encodingFormatter) {
        this.encodingFormatter = encodingFormatter;
    }
    
    protected void addNamedFormat(String name, String pattern) {
        namedFormatters.put(name, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
    }
    
    protected Optional<FormatStyle> lookupFormatStyle(String styleName) {
        if(styleName==null) {
            return Optional.empty();
        }
        return Stream.of(FormatStyle.values())
        .filter(style->style.name().toLowerCase().equals(styleName))
        .findFirst();
    }
    
    protected Optional<DateTimeFormatter> lookupNamedFormatter(String formatName) {
        return Optional.ofNullable(namedFormatters.get(formatName));
    }
    
    protected DateTimeFormatter lookupNamedFormatterElseFail(String formatName) {
        return lookupNamedFormatter(formatName)
                .orElseThrow(()->_Exceptions.noSuchElement("unknown format name %s", formatName));
    }
    
    protected Optional<DateTimeFormatter> formatterFromPattern(String pattern) {
        try {
            return Optional.of(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()));
        } catch (Exception e) {
            log.warn("cannot parse pattern '{}'", pattern, e);
        }
        return Optional.empty();
    }
    
    protected void updateParsers() {
        parsers = Can.ofCollection(namedFormatters.values())
                .map(formatter->{
                    return $->formatter.parse($, query);
                });
    }
    
    protected Optional<DateTimeFormatter> formatterFirstOf(
            final @NonNull Can<Supplier<Optional<DateTimeFormatter>>> formatterProviders) {
        return formatterProviders.stream()
        .map(Supplier::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }
    
    // -- TEMPORAL VALUE FACET

    @Override
    public final T temporalValue(final ManagedObject adapter) {
        return _Casts.uncheckedCast(UnwrapUtil.single(adapter));
    }

    @Override
    public final ManagedObject createValue(final T temporal) {
        return getObjectManager().adapt(temporal);
    }
    
    // -- ENCODER/DECODER

    @Override
    protected String doEncode(final Object object) {
        final T temporal = _Casts.uncheckedCast(object);
        return encodingFormatter.format(temporal);
    }

    @Override
    protected T doRestore(final String data) {
        try {
            return encodingFormatter.parse(data, query);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }
    
    // -- PARSING

    @Override
    protected T doParse(
            final String entry,
            final Object context) {

        T contextTemporal = _Casts.uncheckedCast(context);

        val temporalString = entry.trim().toUpperCase();
        if(contextTemporal != null) {
            val adjusted = TemporalAdjustUtil
                    .parseAdjustment(adjuster, contextTemporal, temporalString);
            if(adjusted!=null) {
                return adjusted;
            }
        }
        return parse(temporalString, parsers);
    }
    
    private T parse(String dateStr, Iterable<Function<String, T>> parsers) {
        for(val parser: parsers) {
            try {
                return parser.apply(dateStr);
            } catch (final IllegalArgumentException e) {
                // continue to next
            }
        }
        val msg = String.format("Not recognised as a %s: %s", 
                super.getAdaptedClass().getName(), 
                dateStr);
        throw new TextEntryParseException(msg);
    }
    
    // -- TITLE

    @Override
    public String titleString(final Object value) {
        if (value == null) {
            return null;
        }
        val temporal = _Casts.<T>uncheckedCast(value);
        return titleString(titleFormatter, temporal);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        val temporal = _Casts.<T>uncheckedCast(value);
        val formatter = DateTimeFormatter.ofPattern(usingMask, Locale.getDefault());
        return titleString(formatter, temporal);
    }
    
    private String titleString(@NonNull DateTimeFormatter formatter, @Nullable T temporal) {
        return temporal != null ? formatter.format(temporal) : "";
    }

    // --

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ ": " + titleFormatter;
    }
    
    
}
