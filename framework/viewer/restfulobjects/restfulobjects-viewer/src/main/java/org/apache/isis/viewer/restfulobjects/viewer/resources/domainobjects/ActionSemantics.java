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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.facets.actions.idempotent.IdempotentFacet;
import org.apache.isis.core.metamodel.facets.actions.queryonly.QueryOnlyFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.restfulobjects.applib.util.Enums;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;

public enum ActionSemantics {

    QUERY_ONLY("invokeQueryOnly"), IDEMPOTENT("invokeIdempotent"), SIDE_EFFECTS("invoke");

    private final String invokeKey;
    private final String name;

    private ActionSemantics(final String invokeKey) {
        this.invokeKey = invokeKey;
        this.name = Enums.enumToCamelCase(this);
    }

    public String getInvokeKey() {
        return invokeKey;
    }

    public boolean isQueryOnly() {
        return this == QUERY_ONLY;
    }

    public boolean isIdempotent() {
        return this == IDEMPOTENT;
    }

    public String getName() {
        return name;
    }

    public static ActionSemantics determine(final ResourceContext resourceContext, final ObjectAction action) {
        if (action.containsFacet(QueryOnlyFacet.class)) {
            return QUERY_ONLY;
        }
        if (action.containsFacet(IdempotentFacet.class)) {
            return IDEMPOTENT;
        }
        return SIDE_EFFECTS;
    }

}
