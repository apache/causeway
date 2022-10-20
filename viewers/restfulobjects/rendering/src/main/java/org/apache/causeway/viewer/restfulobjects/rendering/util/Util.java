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
package org.apache.causeway.viewer.restfulobjects.rendering.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapper;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;

public final class Util {

    private Util(){}

    // //////////////////////////////////////////////////////////////
    // parsing
    // //////////////////////////////////////////////////////////////

    /**
     * Parse {@link java.io.InputStream} to String, else throw exception
     */
    public static String asStringUtf8(final InputStream body) {
        try {
            return _Strings.ofBytes(_Bytes.of(body), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, e, "could not read body");
        }
    }

    /**
     * Parse (body) string to {@link org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation}, else throw exception
     */
    public static JsonRepresentation readAsMap(final String body) {
        if (body == null) {
            return JsonRepresentation.newMap();
        }
        final String bodyTrimmed = body.trim();
        if (bodyTrimmed.isEmpty()) {
            return JsonRepresentation.newMap();
        }
        return read(bodyTrimmed, "body");
    }

    /**
     * Parse (query) string to {@link org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation}, else throw exception
     */
    public static JsonRepresentation readQueryStringAsMap(final String queryString) {
        if (queryString == null) {
            return JsonRepresentation.newMap();
        }
        final String queryStringTrimmed = queryString.trim();
        if (queryStringTrimmed.isEmpty()) {
            return JsonRepresentation.newMap();
        }
        return read(queryStringTrimmed, "query string");
    }

    private static JsonRepresentation read(final String args, final String argsNature) {
        try {
            final JsonRepresentation jsonRepr = JsonMapper.instance().read(args);
            if (!jsonRepr.isMap()) {
                throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, "could not read %s as a JSON map", argsNature);
            }
            return jsonRepr;
        } catch (final JsonParseException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, e, "could not parse %s", argsNature);
        } catch (final JsonMappingException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, e, "could not read %s as JSON", argsNature);
        } catch (final IOException e) {
            throw RestfulObjectsApplicationException.createWithCauseAndMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, e, "could not parse %s", argsNature);
        }
    }


}
