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

import org.apache.causeway.applib.value.semantics.TemporalValueSemantics;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.OffsetCharacteristic;
import org.apache.causeway.applib.value.semantics.TemporalValueSemantics.TemporalCharacteristic;
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

/**
 * Introduced to handle temporal values with zone or offset information
 * based on existing widgets, that do not support zone or offset information.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TemporalDecomposition<T extends Temporal> implements IConverter<T> {
    private static final long serialVersionUID = 1L;

    public static <T extends Temporal> TemporalDecomposition<T> create(final Class<T> type,
            final ScalarModel scalarModel,
            final TemporalValueSemantics<T> temporalValueSemantics,
            final ConverterBasedOnValueSemantics<T> fullConverter) {

        var baseEditingPattern = fullConverter.getEditingPattern();
        //TODO[CAUSEWAY-3489] switch on offsetCharacteristic and temporalCharacteristic to do this properly
        var editingPattern = scalarModel.isEditingMode()
            ? "yyyy-MM-dd HH:mm:ss" // strip 'XXX' or 'VV';
            : baseEditingPattern;

        var tempDecomp = new TemporalDecomposition<>(type, temporalValueSemantics, fullConverter,
                scalarModel.getViewOrEditMode(),
                editingPattern);
        tempDecomp.initFrom(scalarModel.proposedValue());
        return tempDecomp;
    }

    private final @NonNull Class<T> type;
    private final @NonNull TemporalValueSemantics<T> temporalValueSemantics;
    private final @NonNull ConverterBasedOnValueSemantics<T> fullConverter;
    private final @NonNull ViewOrEditMode viewOrEditMode;

    @Getter
    private final String editingPattern;

    private void initFrom(final ManagedValue proposedValue) {
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
    }

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

    private TemporalCharacteristic temporalCharacteristic() {
        return temporalValueSemantics.getTemporalCharacteristic();
    }

    private OffsetCharacteristic offsetCharacteristic() {
        return temporalValueSemantics.getOffsetCharacteristic();
    }

    private String getZoneOrOffsetSuffix() {
        switch (offsetCharacteristic()) {
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

    private ZoneId zoneId;
    public ZoneId getZoneId() {
        //TODO[CAUSEWAY-3489] remove debug line
        System.err.printf("getZoneId %s%n", zoneId);
        return zoneId;
    }
    public void setZoneId(final ZoneId zoneId) {
        this.zoneId = zoneId;
        //TODO[CAUSEWAY-3489] remove debug line
        System.err.printf("setZoneId %s%n", zoneId);
    }

    private ZoneOffset zoneOffset;
    public ZoneOffset getZoneOffset() {
        //TODO[CAUSEWAY-3489] remove debug line
        System.err.printf("getZoneOffset %s%n", zoneOffset);
        return zoneOffset;
    }
    public void setZoneOffset(final ZoneOffset zoneOffset) {
        this.zoneOffset = zoneOffset;
       //TODO[CAUSEWAY-3489] remove debug line
        System.err.printf("setZoneOffset %s%n", zoneOffset);
    }

}
