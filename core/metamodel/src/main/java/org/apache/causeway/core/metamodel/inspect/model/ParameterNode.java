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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
final class ParameterNode implements MMNode {

    @Programmatic
    private final ObjectActionParameter parameter;

    @Override
    public String title() {
        return parameter.getId();
    }

    @Override
    public String iconName() {
        return "";
    }
    
    @Override
    public void putDetails(Details details) {
        details.put("Parameter Index", "" + parameter.getParameterIndex());
        details.put("Friendly Name", parameter.getCanonicalFriendlyName());
        details.put("Element Type", parameter.getElementType().logicalTypeName());
    }

    // -- TREE NODE STUFF

    @Getter @Setter
    private ActionNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {
        return Stream.of(
                MMNodeFactory.facetGroup(parameter.streamFacets(), this));
    }

}

