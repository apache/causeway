/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.Map;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.viewer.RepContext;

import com.google.common.collect.Maps;

public class TypeBuilder extends LinkRepBuilder {
    
    private static Map<String,String> WELL_KNOWN_TYPES =
        mapOf(
            java.lang.String.class.getName(), "string",
            java.lang.Byte.class.getName(), "byte",
            java.lang.Short.class.getName(), "short",
            java.lang.Integer.class.getName(), "int",
            java.lang.Long.class.getName(), "long",
            java.lang.Boolean.class.getName(), "boolean",
            java.lang.Float.class.getName(), "float",
            java.lang.Double.class.getName(), "double",
            java.math.BigInteger.class.getName(), "bigint",
            java.math.BigDecimal.class.getName(), "bigdec"
            );

    public TypeBuilder(RepContext repContext, ObjectSpecification objectSpec) {
        super(repContext, "type", urlFor(objectSpec));
    }
    
    private static String urlFor(ObjectSpecification objectSpec) {
        return "types/application/vnd+" + map(objectSpec.getFullIdentifier());
    }

    private static String map(String className) {
        String wellKnownType = WELL_KNOWN_TYPES.get(className);
        return wellKnownType != null? wellKnownType: className;
    }

    /**
     * Utility to converts varargs into an immutable map. 
     */
    private static Map<String, String> mapOf(String... keyOrValues) {
        Map<String, String> map = Maps.newLinkedHashMap();
        String key = null;
        for(String keyOrValue: keyOrValues) {
            if(key != null) {
                map.put(key, keyOrValue);
                key = null;
            } else {
                key = keyOrValue;
            }
        }
        return Collections.unmodifiableMap(map);
    }

}