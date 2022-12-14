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
package org.apache.causeway.applib.util.schema;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.common.v2.DifferenceDto;
import org.apache.causeway.schema.common.v2.PeriodDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.MetricsDto;
import org.apache.causeway.schema.ixn.v2.ObjectCountsDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class MemberExecutionDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<MemberExecutionDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(MemberExecutionDto.class, opts->opts.allowMissingRootElement(true)));

    public DtoMapper<MemberExecutionDto> dtoMapper() {
        return dtoMapper.get();
    }

    public MetricsDto metricsFor(final MemberExecutionDto executionDto) {
        MetricsDto metrics = executionDto.getMetrics();
        if(metrics == null) {
            metrics = new MetricsDto();
            executionDto.setMetrics(metrics);
        }
        return metrics;
    }

    public PeriodDto timingsFor(final MetricsDto metricsDto) {
        PeriodDto timings = metricsDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            metricsDto.setTimings(timings);
        }
        return timings;
    }

    public ObjectCountsDto objectCountsFor(final MetricsDto metricsDto) {
        ObjectCountsDto objectCounts = metricsDto.getObjectCounts();
        if(objectCounts == null) {
            objectCounts = new ObjectCountsDto();
            metricsDto.setObjectCounts(objectCounts);
        }
        return objectCounts;
    }

    public DifferenceDto numberObjectsLoadedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getLoaded();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setLoaded(differenceDto);
        }
        return differenceDto;
    }
    public DifferenceDto numberObjectsDirtiedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getDirtied();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setDirtied(differenceDto);
        }
        return differenceDto;
    }

}
