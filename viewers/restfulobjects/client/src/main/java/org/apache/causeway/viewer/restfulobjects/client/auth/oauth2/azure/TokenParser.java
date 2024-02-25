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
package org.apache.causeway.viewer.restfulobjects.client.auth.oauth2.azure;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.causeway.commons.functional.Railway;

import lombok.Data;

class TokenParser {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    Railway<IOException,TokenSuccessResponse> parseTokenEntity(final String entity) {
        try {
            return Railway.success(OBJECT_MAPPER.readerFor(TokenSuccessResponse.class).readValue(entity));
        } catch (IOException e) {
            return Railway.failure(e);
        }
    }

    @Data
    static
    class TokenSuccessResponse {
        private String token_type;
        private int expires_in;
        private int ext_expires_in;
        private String access_token;
    }
}
