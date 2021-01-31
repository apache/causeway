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
package org.apache.isis.applib.util.schema;

import java.io.Writer;

import javax.xml.bind.JAXBException;

import org.apache.isis.commons.internal.resources._Xml;
import org.apache.isis.commons.internal.resources._Xml.WriteOptions;
import org.apache.isis.schema.common.v2.DifferenceDto;
import org.apache.isis.schema.common.v2.PeriodDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;
import org.apache.isis.schema.ixn.v2.MetricsDto;
import org.apache.isis.schema.ixn.v2.ObjectCountsDto;

import lombok.NonNull;

/**
 * @since 1.x {@index}
 */
public final class MemberExecutionDtoUtils {

    public static <T extends MemberExecutionDto> T clone(final T dto) {
        return _Xml.clone(dto)
                .orElseFail();
    }

    public static MetricsDto metricsFor(final MemberExecutionDto executionDto) {
        MetricsDto metrics = executionDto.getMetrics();
        if(metrics == null) {
            metrics = new MetricsDto();
            executionDto.setMetrics(metrics);
        }
        return metrics;
    }

    public static PeriodDto timingsFor(final MetricsDto metricsDto) {
        PeriodDto timings = metricsDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            metricsDto.setTimings(timings);
        }
        return timings;
    }

    public static ObjectCountsDto objectCountsFor(final MetricsDto metricsDto) {
        ObjectCountsDto objectCounts = metricsDto.getObjectCounts();
        if(objectCounts == null) {
            objectCounts = new ObjectCountsDto();
            metricsDto.setObjectCounts(objectCounts);
        }
        return objectCounts;
    }

    public static DifferenceDto numberObjectsLoadedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getLoaded();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setLoaded(differenceDto);
        }
        return differenceDto;
    }
    public static DifferenceDto numberObjectsDirtiedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getDirtied();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setDirtied(differenceDto);
        }
        return differenceDto;
    }

    public static <T extends MemberExecutionDto> String toXml(final @NonNull T dto) {
        return _Xml.writeXml(dto, writeOptions())
                .orElseFail();
    }

    public static <T extends MemberExecutionDto> void toXml(
            final @NonNull T dto,
            final @NonNull Writer writer) throws JAXBException {
        _Xml.writeXml(dto, writer, writeOptions());
    }

    // -- HELPER

    private static WriteOptions writeOptions() {
        return WriteOptions.builder()
                .useContextCache(true)
                .formattedOutput(true)
                .allowMissingRootElement(true)
                .build();
    }


}
