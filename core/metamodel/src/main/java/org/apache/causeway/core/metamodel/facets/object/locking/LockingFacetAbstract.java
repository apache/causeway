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
package org.apache.causeway.core.metamodel.facets.object.locking;

import java.util.function.BiConsumer;

import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;

public abstract class LockingFacetAbstract extends FacetAbstract implements LockingFacet {

    @Getter
    private final LockingPolicy policy;

    protected LockingFacetAbstract(final FacetHolder holder, final LockingPolicy policy) {
        super(LockingFacet.class, holder);
        this.policy = policy;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        LockingFacet.super.visitAttributes(visitor);
        visitor.accept("policy", policy);
    }
}
