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

package org.apache.isis.core.metamodel.facets.object.value;

import java.util.Map;

import org.apache.isis.applib.adapters.Parser2;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class MaxLengthFacetUsingParser2 extends MaxLengthFacetAbstract{

    private final Parser2<?> parser;
    private final ServicesInjector dependencyInjector;

    public MaxLengthFacetUsingParser2(final Parser2<?> parser, final FacetHolder holder, final ServicesInjector dependencyInjector) {
        super(parser.maxLength(), holder);
        this.parser = parser;
        this.dependencyInjector = dependencyInjector;
    }

    @Override
    protected String toStringValues() {
        getDependencyInjector().injectServicesInto(parser);
        return parser.toString();
    }

    @Override
    public int value() {
        getDependencyInjector().injectServicesInto(parser);
        return parser.maxLength();
    }

    @Override
    public String toString() {
        return "maxLength=" + value();
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////

    /**
     * @return the dependencyInjector
     */
    public ServicesInjector getDependencyInjector() {
        return dependencyInjector;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("parser", parser);
    }
}
