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

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.common.v2.DifferenceDto;
import org.apache.causeway.schema.common.v2.PeriodDto;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;
import org.apache.causeway.schema.ixn.v2.MemberExecutionDto;
import org.apache.causeway.schema.ixn.v2.MetricsDto;
import org.apache.causeway.schema.ixn.v2.ObjectCountsDto;
import org.apache.causeway.schema.ixn.v2.PropertyEditDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class MemberExecutionDtoUtils {

    public void init() {
        dtoMapperForActionInvocation.get();
        dtoMapperForPropertyEdit.get();
    }

    private _Lazy<DtoMapper<ActionInvocationDto>> dtoMapperForActionInvocation = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(ActionInvocationDto.class, opts->opts.allowMissingRootElement(true)));

    private _Lazy<DtoMapper<PropertyEditDto>> dtoMapperForPropertyEdit = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(PropertyEditDto.class, opts->opts.allowMissingRootElement(true)));

    public static <T extends MemberExecutionDto> DtoMapper<MemberExecutionDto> dtoMapper(final Class<T> dtoClass) {
        return _Casts.uncheckedCast(
                ActionInvocationDto.class.equals(dtoClass)
                    ? dtoMapperForActionInvocation.get()
                    : dtoMapperForPropertyEdit.get());
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
