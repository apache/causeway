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
import lombok.experimental.UtilityClass;

@UtilityClass
class _OpenApi {

    @SuppressWarnings("rawtypes")
    Schema schema(final String typeLiteral) {
        switch(typeLiteral) {
        case "string":
            return new StringSchema();
        default:
            return new Schema().type(typeLiteral);
        }
    }

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

    MediaType mediaType(final ObjectSchema schema) {
        return new MediaType().schema(schema);
    }

    RequestBody requestBody(final String mimeLiteral, final ObjectSchema bodySchema) {
        return new RequestBody()
        .content(new Content()
                .addMediaType(mimeLiteral, mediaType(bodySchema)));
    }

    //TODO[ISIS-3292] honor schema
    ApiResponse response(final Schema schema) {
        return new ApiResponse();
    }

    Operation produces(final Operation operation, final String string) {
        // TODO[ISIS-3292] Auto-generated method stub
        return operation;
    }

    Operation response(final Operation operation, final int code, final ApiResponse response) {
        // TODO[ISIS-3292] Auto-generated method stub
        return operation;
    }

    // -- CUSTOM TYPES

    private static class RefSchema extends Schema<Object> {
        public RefSchema(final String schemaRefLiteral) {
            super("ref", null);
            super.set$ref(schemaRefLiteral);
        }
    }
    @SuppressWarnings("rawtypes")
    Schema refSchema(final String schemaRefLiteral) {
        return new RefSchema(schemaRefLiteral);
    }

    // -- NOT USED

//  Link link(final String schemaRefLiteral) {
//  return new Link().$ref(schemaRefLiteral);
//}

}
