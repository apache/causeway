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
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public abstract sealed class MemberNode
implements MMNode
permits
    ActionNode,
    CollectionNode,
    PropertyNode {

    // -- MIXIN STUFF

    @Getter @Setter @Accessors(makeFinal = true)
    private boolean mixedIn;

    @Override
    public final String iconName() {
        return isMixedIn() ? "mixedin" : "";
    }

    protected final String titleSuffix() {
        return isMixedIn() ? " (mixed in)" : "";
    }

    // -- TREE NODE STUFF

    @Getter @Setter @Accessors(makeFinal = true)
    private TypeNode parentNode;

    @Programmatic
    @Override
    public Stream<MMNode> streamChildNodes() {
        return Stream.of(
                MMNodeFactory.facetGroup(member().streamFacets(), this));
    }

    @Programmatic
    protected abstract ObjectMember member();

}

