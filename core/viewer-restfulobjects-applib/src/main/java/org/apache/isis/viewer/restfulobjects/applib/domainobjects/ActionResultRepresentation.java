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

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import com.fasterxml.jackson.databind.JsonNode;

public class ActionResultRepresentation extends AbstractObjectMemberRepresentation {

    public enum ResultType {
        DOMAIN_OBJECT("domainobject"), LIST("list"), SCALAR_VALUE("scalarvalue"), VOID("void");

        private final String value;

        ResultType(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ResultType lookup(final String value) {
            for (final ResultType resultType : values()) {
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
