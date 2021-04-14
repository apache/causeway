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
package org.apache.isis.viewer.restfulobjects.applib.domainobjects;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 1.x {@index}
 */
public class ActionResultRepresentation extends AbstractObjectMemberRepresentation {

    @RequiredArgsConstructor
    public enum ResultType {
        DOMAIN_OBJECT("domainobject"),
        LIST("list"),
        SCALAR_VALUE("scalarvalue"),

        /**
         * Variant of 'list' representing a list of scalar values.
         * NOT supported by the RO spec v1.0, but allows for custom representations to
         * support this particular data structure.
         * @since 2.0
         */
        SCALAR_VALUES("scalarvalues"),

        VOID("void");

        @Getter private final String value;

        public static ResultType lookup(final String value) {
            for (val resultType : values()) {
                if (resultType.value.equals(value)) {
                    return resultType;
                }
            }
            throw new IllegalArgumentException("Value '" + value + "' is not a valid result type");
        }

        public boolean isVoid() {
            return this == VOID;
        }
    }

    public ActionResultRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    public JsonRepresentation getResult() {
        return getRepresentation("result");
    }

    public ResultType getResultType() {
        final String resultType = getString("resulttype");
        return ResultType.lookup(resultType);
    }
}
