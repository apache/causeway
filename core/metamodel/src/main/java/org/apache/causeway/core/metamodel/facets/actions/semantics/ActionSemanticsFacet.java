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
package org.apache.causeway.core.metamodel.facets.actions.semantics;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Represents the semantics of an action.
 *
 * <p> Specifically, whether it is safe, idempotent or non-idempotent.
 */
public final class ActionSemanticsFacet extends FacetAbstract {

	@Getter @Accessors(fluent = true)
    private final String origin;
	@Getter @Accessors(fluent = true)
    private final SemanticsOf value;

    public ActionSemanticsFacet(final String origin, final SemanticsOf of, final FacetHolder facetHolder) {
    	super(ActionSemanticsFacet.class, facetHolder);
        this.origin = origin;
    	this.value = of;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	super.visitAttributes(visitor);
        visitor.accept("origin", origin);
        visitor.accept("value", value);
    }

}
