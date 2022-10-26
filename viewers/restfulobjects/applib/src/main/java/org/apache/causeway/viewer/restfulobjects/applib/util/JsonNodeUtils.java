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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

/**
 * @since 1.x {@index}
 */
public class JsonNodeUtils {

    private JsonNodeUtils() {
    }

    public static InputStream asInputStream(final JsonNode jsonNode) {
        final String jsonStr = jsonNode.toString();
        final byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream asInputStream(final JsonRepresentation jsonRepresentation) {
        final String jsonStr = jsonRepresentation.toString();
        final byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Walks the path, ensuring keys exist and are maps, or creating required
     * maps as it goes.
     *
     * <p>
     * For example, if given a list ("a", "b", "c") and starting with an empty
     * map, then will create:
     *
     * <pre>
     * {
     *   "a": {
     *     "b: {
     *       "c": {
     *       }
     *     }
     *   }
     * }
     */
    public static ObjectNode walkNodeUpTo(ObjectNode node, final List<String> keys) {
        for (final String key : keys) {
            JsonNode jsonNode = node.get(key);
            if (jsonNode == null) {
                jsonNode = new ObjectNode(JsonNodeFactory.instance);
                node.set(key, jsonNode);
            } else {
                if (!jsonNode.isObject()) {
                    throw new IllegalArgumentException(String.format("walking path: '%s', existing key '%s' is not a map", keys, key));
                }
            }
            node = (ObjectNode) jsonNode;
        }
        return node;
    }

}
