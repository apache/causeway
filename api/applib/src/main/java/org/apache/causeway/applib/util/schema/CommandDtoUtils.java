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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.MapDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.PeriodDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class CommandDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<CommandDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(CommandDto.class));

    public DtoMapper<CommandDto> dtoMapper() {
        return dtoMapper.get();
    }

    public OidsDto targetsFor(final CommandDto dto) {
        OidsDto targets = dto.getTargets();
        if(targets == null) {
            targets = new OidsDto();
            dto.setTargets(targets);
        }
        return targets;
    }

    public ParamsDto parametersFor(final ActionDto actionDto) {
        ParamsDto parameters = actionDto.getParameters();
        if(parameters == null) {
            parameters = new ParamsDto();
            actionDto.setParameters(parameters);
        }
        return parameters;
    }

    public PeriodDto timingsFor(final CommandDto commandDto) {
        PeriodDto timings = commandDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            commandDto.setTimings(timings);
        }
        return timings;
    }

    public String getUserData(final CommandDto dto, final String key) {
        if(dto == null || key == null) {
            return null;
        }
        return CommonDtoUtils.getMapValue(dto.getUserData(), key);
    }

    public void setUserData(
            final CommandDto dto, final String key, final String value) {
        if(dto == null || key == null || _Strings.isNullOrEmpty(value)) {
            return;
        }
        final MapDto userData = userDataFor(dto);
        CommonDtoUtils.putMapKeyValue(userData, key, value);
    }

    public void setUserData(
            final CommandDto dto, final String key, final Bookmark bookmark) {
        if(dto == null || key == null || bookmark == null) {
            return;
        }
        setUserData(dto, key, bookmark.toString());
    }

    public void clearUserData(
            final CommandDto dto, final String key) {
        if(dto == null || key == null) {
            return;
        }
        userDataFor(dto).getEntry().removeIf(x -> x.getKey().equals(key));
    }

    private MapDto userDataFor(final CommandDto commandDto) {
        MapDto userData = commandDto.getUserData();
        if(userData == null) {
            userData = new MapDto();
            commandDto.setUserData(userData);
        }
        return userData;
    }

}
