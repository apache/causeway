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
package org.apache.isis.extensions.restclient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;

/**
 * 
 * @since 2.0
 */
public class ActionParameterListBuilder {

    private final Map<String, String> actionParameters = new LinkedHashMap<>();

    public ActionParameterListBuilder addActionParameter(String parameterName, String parameterValue) {
        actionParameters.put(parameterName, parameterValue != null 
                ? value("\"" + parameterValue + "\"") 
                        : value(JSON_NULL_LITERAL));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, int parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, long parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, byte parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, short parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, double parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, float parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

    public ActionParameterListBuilder addActionParameter(String parameterName, boolean parameterValue) {
        actionParameters.put(parameterName, value(""+parameterValue));
        return this;
    }

//XXX would be nice to have, but also requires the RO spec to be updated     
//    public ActionParameterListBuilder addActionParameterDto(String parameterName, Object parameterDto) {
//        actionParameters.put(parameterName, dto(parameterDto));
//        return this;
//    }
    
    public Entity<String> build() {

        final StringBuilder sb = new StringBuilder();
        sb.append("{\n")
        .append(actionParameters.entrySet().stream()
                .map(this::toJson)
                .collect(Collectors.joining(",\n")))
        .append("\n}");

        return Entity.json(sb.toString());
    }

    // -- HELPER

    private static final String JSON_NULL_LITERAL = "null";

    private String value(String valueLiteral) {
        return "{\"value\" : " + valueLiteral + "}";
    }
    
//    @SneakyThrows
//    private String dto(Object dto) {
//        val mapper = new ObjectMapper();
//        return mapper.writeValueAsString(dto);
//    }

    private String toJson(Map.Entry<String, String> entry) {
        return "   \""+entry.getKey()+"\": "+entry.getValue();
    }


}
