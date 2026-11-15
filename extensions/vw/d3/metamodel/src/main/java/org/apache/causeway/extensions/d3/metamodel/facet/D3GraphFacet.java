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
package org.apache.causeway.extensions.d3.metamodel.facet;

import java.util.Optional;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.extensions.d3.applib.config.D3Config;

public interface D3GraphFacet extends Facet {

    D3Config configFor(Class<?> domainClass);

    /**
     * Optionally the member id of a domain action to invoke when a graph node is clicked,
     * resolved for given {@code domainClass} (annotation or advisor based).
     */
    Optional<String> getClickAction(Class<?> domainClass);

}

