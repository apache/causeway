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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.json.JsonParseException;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.restfulobjects.applib.util.JsonMapperUtil;

import tools.jackson.databind.JsonNode;

public class JsonFixture {

    private JsonFixture() {
    }

    public static JsonNode readJson(final String resourceName)
            throws JsonParseException, IOException {

        var json = _Strings.read(JsonFixture.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8);

        return JsonMapperUtil.instance().read(json, JsonNode.class);
    }

}
