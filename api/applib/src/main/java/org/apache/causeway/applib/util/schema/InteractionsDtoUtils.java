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

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.DtoMapper;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.schema.ixn.v2.InteractionsDto;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class InteractionsDtoUtils {

    public void init() {
        dtoMapper.get();
    }

    private _Lazy<DtoMapper<InteractionsDto>> dtoMapper = _Lazy.threadSafe(
            ()->JaxbUtils.mapperFor(InteractionsDto.class));

    public DtoMapper<InteractionsDto> dtoMapper() {
        return dtoMapper.get();
    }

    // -- other

    public static List<InteractionDto> split(final InteractionsDto interactionsDto) {
        List<InteractionDto> interactionDtos = new ArrayList<>();
        interactionsDto.getInteractionDto().forEach(interactionDto -> {
            copyVersion(interactionsDto, interactionDto);
            interactionDtos.add(interactionDto);
        });
        return interactionDtos;
    }

    private static void copyVersion(
            final InteractionsDto from,
            final InteractionDto to) {
        var majorVersion = from.getMajorVersion();
        var minorVersion = from.getMinorVersion();
        if (!_Strings.isNullOrEmpty(majorVersion) && !_Strings.isNullOrEmpty(minorVersion)) {
            to.setMajorVersion(majorVersion);
            to.setMinorVersion(minorVersion);
        }
    }

    public static InteractionsDto join(
            final List<InteractionDto> interactionDtos) {
        var interactionsDto = new InteractionsDto();
        interactionDtos.forEach(interactionDto -> {
            copyVersion(interactionDto, interactionsDto);
            interactionsDto.getInteractionDto().add(interactionDto);
        });
        return interactionsDto;
    }

    private static void copyVersion(
            final InteractionDto from,
            final InteractionsDto dto) {
        var majorVersion = from.getMajorVersion();
        var minorVersion = from.getMinorVersion();
        if (!_Strings.isNullOrEmpty(majorVersion) && !_Strings.isNullOrEmpty(minorVersion)) {
            dto.setMajorVersion(majorVersion);
            dto.setMinorVersion(minorVersion);
        }
        from.setMajorVersion(null);
        from.setMinorVersion(null);
    }

}
