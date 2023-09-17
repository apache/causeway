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
package org.apache.causeway.viewer.restfulobjects.applib.util;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.experimental.UtilityClass;

/**
 * @since 1.x {@index}
 */
@UtilityClass
public final class UrlEncodingUtils {

    public String urlDecode(final String string) {
        return UrlUtils.urlDecodeUtf8(string);
    }

    public List<String> urlDecode(final List<String> values) {
        return _Lists.map(values, UrlUtils::urlDecodeUtf8);
    }

    public String[] urlDecode(final String[] values) {
        final List<String> asList = Arrays.asList(values);
        return urlDecode(asList).toArray(new String[] {});
    }

    public String urlEncode(final JsonNode jsonNode) {
        return urlEncode(jsonNode.toString());
    }

    public String urlEncode(final JsonRepresentation jsonRepresentation ) {
        return urlEncode(jsonRepresentation.toString());
    }

    public String urlEncode(final String str) {
        return UrlUtils.urlEncodeUtf8(str);
    }

}
