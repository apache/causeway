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
package org.apache.causeway.viewer.restfulobjects.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.client.RepresentationTypeSimplifiedV2;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.viewer.restfulobjects.applib.dtos.ScalarValueDtoV2;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

interface ResponseDigester {

    <T> T readSingle(Class<T> entityType, Response response);
    <T> List<T> readList(Class<T> entityType, GenericType<List<T>> genericType, Response response);

    // -- FACTORIES

    static Optional<ResponseDigester> forContentTypeHeaderString(final @Nullable String contentTypeHeaderString) {
        if(_Strings.isEmpty(contentTypeHeaderString)) {
            return Optional.empty();
        }
        if(contentTypeHeaderString.startsWith("application/xml;profile=\"urn:org.restfulobjects:repr-types/action-result\"")
                && contentTypeHeaderString.contains("x-ro-domain-type")) {
            return Optional.of(new ResponseDigesterXmlStandard());
        }
        return RepresentationTypeSimplifiedV2.parseContentTypeHeaderString(contentTypeHeaderString)
              .map(ResponseDigesterJsonSimple::new);
    }

    // -- IMPLEMENTATIONS

    @RequiredArgsConstructor
    static class ResponseDigesterXmlStandard implements ResponseDigester {

        @Override
        public <T> T readSingle(final Class<T> entityType, final Response response) {
            return response.readEntity(entityType);
        }

        @Override
        public <T> List<T> readList(final Class<T> entityType, final GenericType<List<T>> genericType, final Response response) {
            throw _Exceptions.notImplemented();
        }
    }

    @RequiredArgsConstructor
    static class ResponseDigesterJsonSimple implements ResponseDigester {

        private final RepresentationTypeSimplifiedV2 reprType;

        @SneakyThrows
        @Override
        public <T> T readSingle(final Class<T> entityType, final Response response) {
            if(reprType.isValue()
                    || reprType.isValues()) {
                val mapper = new ObjectMapper();
                val jsonInput = response.readEntity(String.class);
                val scalarValueDto = mapper.readValue(jsonInput, ScalarValueDtoV2.class);
                return extractValue(scalarValueDto);
            }
            return response.<T>readEntity(entityType);
        }

        @SneakyThrows
        @Override
        public <T> List<T> readList(final Class<T> entityType, final GenericType<List<T>> genericType, final Response response) {
            if(reprType.isValues()
                    || reprType.isValue()) {
                val mapper = new ObjectMapper();
                val jsonInput = response.readEntity(String.class);
                final List<ScalarValueDtoV2> scalarValueDtoList =
                        mapper.readValue(
                                jsonInput,
                                mapper.getTypeFactory().constructCollectionType(List.class, ScalarValueDtoV2.class));

                final List<T> resultList = new ArrayList<>(scalarValueDtoList.size());
                for(val valueBody : scalarValueDtoList) {
                    // explicit loop, for simpler exception propagation
                    resultList.add(extractValue(valueBody));
                }
                return resultList;

            }
            return response.readEntity(genericType);
        }

        private <T> T extractValue(final ScalarValueDtoV2 scalarValueDto)
                throws JsonParseException, JsonMappingException, IOException {
            return _Casts.uncheckedCast(scalarValueDto.getValue());
        }
    }

}
