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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("isis.val.OffsetDateTimeValueSemantics")
//@Log4j2
public class OffsetDateTimeValueSemantics
extends TemporalValueSemanticsProvider<OffsetDateTime> {

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = 22;

    @Override
    public Class<OffsetDateTime> getCorrespondingClass() {
        return OffsetDateTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.OFFSET_DATE_TIME;
    }

    public OffsetDateTimeValueSemantics(final IsisConfiguration config) {
        super(TemporalCharacteristic.DATE_TIME, OffsetCharacteristic.OFFSET,
                TYPICAL_LENGTH, MAX_LENGTH,
                OffsetDateTime::from,
                TemporalAdjust::adjustOffsetDateTime);

        val basicDateTimeNoMillis = "yyyyMMdd'T'HHmmssZ";
        val basicDateTime = "yyyyMMdd'T'HHmmss.SSSZ";

        super.addNamedFormat("iso", basicDateTimeNoMillis);
        super.addNamedFormat("iso_encoding", basicDateTime);

        super.updateParsers();

        setEncodingFormatter(DateTimeFormatter.ofPattern(basicDateTime, Locale.getDefault()));

        val configuredNameOrPattern = config.getValueTypes().getJavaTime().getOffsetDateTime().getFormat();

        // walk through 3 methods of generating a formatter, first one to return non empty wins
        val formatter = formatterFirstOf(Can.of(
                ()->lookupFormatStyle(configuredNameOrPattern).map(DateTimeFormatter::ofLocalizedDateTime),
                ()->lookupNamedFormatter(configuredNameOrPattern),
                ()->formatterFromPattern(configuredNameOrPattern)
                ))
        .orElseGet(()->DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));  // fallback

        //TODO those FormatStyle based formatters potentially need additional zone information
        setTitleFormatter(formatter);

    }

}
