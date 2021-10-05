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
package org.apache.isis.core.metamodel.valuesemantics.temporal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.LocalDateValueSemantics")
//@Log4j2
public class LocalDateValueSemantics
extends TemporalValueSemanticsProvider<LocalDate> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    @Override
    public Class<LocalDate> getCorrespondingClass() {
        return LocalDate.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.LOCAL_DATE;
    }

    public LocalDateValueSemantics(final FacetHolder holder) {
        super(TemporalCharacteristic.DATE_ONLY, OffsetCharacteristic.LOCAL,
                TYPICAL_LENGTH, MAX_LENGTH,
                LocalDate::from,
                TemporalAdjust::adjustLocalDate);

        super.addNamedFormat("iso", "yyyy-MM-dd");
        super.addNamedFormat("iso_encoding", "yyyy-MM-dd");
        super.updateParsers();

        setEncodingFormatter(lookupNamedFormatterElseFail("iso_encoding"));

        val configuredNameOrPattern = config.getValueTypes().getJavaTime().getLocalDate().getFormat();

        // walk through 3 methods of generating a formatter, first one to return non empty wins
        val formatter = formatterFirstOf(Can.of(
                ()->lookupFormatStyle(configuredNameOrPattern).map(DateTimeFormatter::ofLocalizedDate),
                ()->lookupNamedFormatter(configuredNameOrPattern),
                ()->formatterFromPattern(configuredNameOrPattern)
                ))
        .orElseGet(()->DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));  // fallback

        setTitleFormatter(formatter);

    }

}
