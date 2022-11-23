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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.List;
import java.util.function.Consumer;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _OpenApi {

    /** defaulting to type: string */
    Parameter pathParameter() {
        return new PathParameter()
                .schema(new StringSchema());
    }

    /** defaulting to type: string */
    Parameter queryParameter() {
        return new QueryParameter()
                .schema(new StringSchema());
    }

    MediaType mediaType(final Schema<?> schema) {
        return new MediaType().schema(schema);
    }

    RequestBody requestBody(final String mimeLiteral, final ObjectSchema bodySchema) {
        return new RequestBody()
        .content(new Content()
                .addMediaType(mimeLiteral, mediaType(bodySchema)))
        .required(false);
    }

    Operation operation(
            final int responseCode,
            final Schema<?> ref,
            final @NonNull List<String> supportedFormats,
            final Consumer<ApiResponse> responseRefiner) {
        val content = new Content();
        supportedFormats
            .forEach(format->
                content.addMediaType(format, mediaType(ref)));
        val response = new ApiResponse()
                .content(content);
        responseRefiner.accept(response);
        return new Operation()
        .responses(new ApiResponses()
                .addApiResponse("" + responseCode, response));
    }

    ApiResponse withCacheControl(final ApiResponse response, final Caching caching) {
        return caching.header()
                .map(header->response.addHeaderObject("Cache-Control", header))
                .orElse(response);
    }

    // -- CUSTOM TYPES

    private static class RefSchema extends Schema<Object> {
        public RefSchema(final String schemaRefLiteral) {
            super("ref", null);
            super.set$ref("#/components/schemas/" + schemaRefLiteral);
        }
    }
    Schema<Object> refSchema(final String schemaRefLiteral) {
        return new RefSchema(schemaRefLiteral);
    }

    // -- NOT USED

//    @SuppressWarnings("rawtypes")
//    Schema schema(final String typeLiteral) {
//        switch(typeLiteral) {
//        case "string":
//            return new StringSchema();
//        default:
//            return new Schema().type(typeLiteral);
//        }
//    }

//    Link link(final String schemaRefLiteral) {
//        return new Link().$ref(schemaRefLiteral);
//    }

}
