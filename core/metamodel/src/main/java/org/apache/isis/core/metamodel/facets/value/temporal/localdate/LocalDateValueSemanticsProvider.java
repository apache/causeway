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

package org.apache.isis.core.metamodel.facets.value.temporal.localdate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalAdjust;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueSemanticsProviderAbstract;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalDateValueSemanticsProvider
extends TemporalValueSemanticsProviderAbstract<LocalDate> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    public LocalDateValueSemanticsProvider(final FacetHolder holder) {
        super(TemporalValueFacet.class, 
                TemporalCharacteristic.DATE_ONLY, OffsetCharacteristic.LOCAL,  
                holder, LocalDate.class, TYPICAL_LENGTH, MAX_LENGTH,
                LocalDate::from,
                TemporalAdjust::adjustLocalDate);
        
        super.addNamedFormat("long", "L-");
        super.addNamedFormat("medium", "M-");
        super.addNamedFormat("short", "S-");
        super.addNamedFormat("iso", "yyyy-MM-dd");
        super.addNamedFormat("iso_encoding", "yyyy-MM-dd");
        super.updateParsers();

        setEncodingFormatter(lookupNamedFormatterElseFail("iso_encoding"));

        val configuredNameOrPattern = getConfiguration().getValueTypes().getJavaTime().getLocalDate().getFormat();

        val formatter = lookupNamedFormatter(configuredNameOrPattern).orElse(null);

        setTitleFormatter(formatter != null ? formatter : formatterFrom(configuredNameOrPattern));
    }

    private DateTimeFormatter formatterFrom(String configuredNameOrPattern) {
        DateTimeFormatter result = null;
        try {
            result = DateTimeFormatter.ofPattern(configuredNameOrPattern, Locale.getDefault());
        } catch (Exception e) {
            log.warn(e);
        }
        if (result == null) {
            result = lookupNamedFormatterElseFail("medium");
        }
        return result;
    }


}
