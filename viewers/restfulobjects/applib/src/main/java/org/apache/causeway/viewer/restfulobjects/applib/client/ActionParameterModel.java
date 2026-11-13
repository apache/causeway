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
package org.apache.causeway.viewer.restfulobjects.applib.client;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;

public interface ActionParameterModel {
    Map<String, Class<?>> getActionParameterTypes();

    ActionParameterModel addActionParameter(String parameterName, String parameterValue);

    ActionParameterModel addActionParameter(String parameterName, int parameterValue);

    ActionParameterModel addActionParameter(String parameterName, long parameterValue);

    ActionParameterModel addActionParameter(String parameterName, byte parameterValue);

    ActionParameterModel addActionParameter(String parameterName, short parameterValue);

    ActionParameterModel addActionParameter(String parameterName, double parameterValue);

    ActionParameterModel addActionParameter(String parameterName, float parameterValue);

    ActionParameterModel addActionParameter(String parameterName, boolean parameterValue);

    ActionParameterModel addActionParameter(String parameterName, Blob blob);

    ActionParameterModel addActionParameter(String parameterName, Clob clob);

    ActionParameterModel addActionParameter(String parameterName, Map<String, Object> map);

    ActionParameterModel addActionParameter(String parameterName, Bookmark bookmark);

    <T> ActionParameterModel addActionParameter(String parameterName, Class<T> type, T object);

    /**
     * For transport of {@link ValueDecomposition} over REST.
     */
    ActionParameterModel addActionParameter(String parameterName, ValueDecomposition decomposition);

    static ActionParameterModel create(String baseUrl) {
        return new ActionParameterModelRecord(baseUrl, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    String toJson();

}