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
package org.apache.isis.testing.unittestsupport.applib.dom.repo;

import java.util.Map;

import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;

public class FinderInteraction {
    public enum FinderMethod {
        FIRST_MATCH,
        ALL_MATCHES,
        ALL_INSTANCES,
        UNIQUE_MATCH
    }
    private NamedQuery<?> queryDefault;
    private FinderInteraction.FinderMethod finderMethod;
    public FinderInteraction(Query<?> query, FinderInteraction.FinderMethod finderMethod) {
        super();
        this.queryDefault = (NamedQuery<?>) query;
        this.finderMethod = finderMethod;
    }
    public NamedQuery<?> getQueryDefault() {
        return queryDefault;
    }
    public FinderInteraction.FinderMethod getFinderMethod() {
        return finderMethod;
    }
    public Class<?> getResultType() {
        return queryDefault.getResultType();
    }
    public String getQueryName() {
        return queryDefault.getName();
    }
    public Map<String, Object> getArgumentsByParameterName() {
        return queryDefault.getParametersByName();
    }
    public int numArgs() {
        return queryDefault.getParametersByName().size();
    }
}