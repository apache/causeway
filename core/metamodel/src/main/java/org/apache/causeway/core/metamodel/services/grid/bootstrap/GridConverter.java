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
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.converter.Converter;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGridDto;
import org.apache.causeway.applib.layout.grid.bootstrap.BSUtil;

@FunctionalInterface
interface GridConverter<S, T> extends Converter<S, T> {

    record FromDto(Class<?> domainClass) implements GridConverter<BSGridDto, BSGrid> {

        @Override
        public @Nullable BSGrid convert(final BSGridDto orig) {
            var dto = BSUtil.deepCopy(orig);
            var grid = new BSGrid(domainClass);
            grid.getRows().addAll(dto.getRows());
            grid.getMetadataErrors().addAll(dto.getMetadataErrors());
            dto.getRows().clear();
            dto.getMetadataErrors().clear();
            BSUtil.setupOwnerPointers(grid);
            return grid;
        }

    }

    record ToDto() implements GridConverter<BSGrid, BSGridDto> {

        @Override
        public @Nullable BSGridDto convert(final BSGrid orig) {
            var grid = BSUtil.deepCopy(orig);
            var dto = new BSGridDto();
            dto.getRows().addAll(grid.getRows());
            dto.getMetadataErrors().addAll(grid.getMetadataErrors());
            return dto;
        }

    }

}
