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
package org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.value.semantics.TemporalCharacteristicsProvider.OffsetCharacteristic;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Introduced to handle temporal values with zone or offset information
 * based on existing widgets, that do not support zone or offset information.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TemporalDecomposition<T> implements IConverter<T> {
    private static final long serialVersionUID = 1L;

    public static <T extends Temporal> TemporalDecomposition<T> create(final Class<T> type,
            final ScalarModel scalarModel,
            final OffsetCharacteristic offsetCharacteristic,
            final ConverterBasedOnValueSemantics<T> fullConverter) {

        _Assert.assertTrue(fullConverter.canHandle(type));

        var needsDecomposition = !offsetCharacteristic.isLocal()
                && scalarModel.getViewOrEditMode().isEditing();

        var baseEditingPattern = fullConverter.getEditingPattern();
        var editingPattern = needsDecomposition
            ? stripZoneSuffixFrom(baseEditingPattern)
            : baseEditingPattern;

        var tempDecomp = new TemporalDecomposition<>(type, offsetCharacteristic, fullConverter,
                scalarModel.getViewOrEditMode(),
                editingPattern);

        if(needsDecomposition) {
            tempDecomp.initFrom(scalarModel.proposedValue());
        }
        return tempDecomp;
    }

    private final @NonNull Class<T> type;
    private final OffsetCharacteristic offsetCharacteristic;
    private final @NonNull ConverterBasedOnValueSemantics<T> fullConverter;
    private final @NonNull ViewOrEditMode viewOrEditMode;

    @Getter
    private final String editingPattern;

    private void initFrom(final ManagedValue proposedValue) {
        //TODO[CAUSEWAY-3489] handle non java.time.Temporal values also
        var temporalValue = MmUnwrapUtils.single(proposedValue.getValue().getValue());
        if(temporalValue instanceof ZonedDateTime) {
            var zonedDateTime = (ZonedDateTime) temporalValue;
            this.zoneId = zonedDateTime.getZone();
        } else if(temporalValue instanceof OffsetDateTime) {
            var offsetDateTime = (OffsetDateTime) temporalValue;
            this.zoneOffset = offsetDateTime.getOffset();
        } else if(temporalValue instanceof OffsetTime) {
            var offsetTime = (OffsetTime) temporalValue;
            this.zoneOffset = offsetTime.getOffset();
        }
        //TODO[CAUSEWAY-3489] zone/offset info on new temporal value should be set to current user's info from session
    }

    // -- CONVERTER

    @Override
    public T convertToObject(final String noZoneValue, final Locale locale) throws ConversionException {
        var fullTemporalString = noZoneValue + getZoneOrOffsetSuffix();
        return fullConverter.convertToObject(fullTemporalString, locale);
    }
    @Override
    public String convertToString(final T value, final Locale locale) {
        return fullConverter.convertToString(value, locale);
    }

    // -- SPECIALIZATION

    private String getZoneOrOffsetSuffix() {
        switch (offsetCharacteristic) {
        case OFFSET:
            return " " + _Temporals.formatZoneId(zoneOffset==null
                    ? ZoneOffset.UTC
                    : zoneOffset,
                _Temporals.ISO_OFFSET_ONLY_FORMAT);
        case ZONED:
            return " " + _Temporals.formatZoneId(zoneId==null
                    ? ZoneOffset.UTC
                    : zoneId);
        default:
            return "";
        }
    }

    // -- PROPERTIES

    @Getter @Setter private ZoneId zoneId;
    @Getter @Setter private ZoneOffset zoneOffset;

    // -- HELPER

    /**
     * Strips 'XXX' and 'VV' from end of pattern. Does not understand other zone/offset formats.
     */
    private static String stripZoneSuffixFrom(final String pattern) {
        if(pattern.endsWith("XXX")) {
            return _Strings.substring(pattern, 0, -3).stripTrailing();
        }
        if(pattern.endsWith("VV")) {
            return _Strings.substring(pattern, 0, -2).stripTrailing();
        }
        return pattern;
    }

}
