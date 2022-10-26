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
package org.apache.causeway.core.metamodel.facets.objectvalue.mandatory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.causeway.core.metamodel.interactions.PropertyModifyContext;
import org.apache.causeway.core.metamodel.interactions.ProposedHolder;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class MandatoryFacetAbstract
extends FacetAbstract
implements MandatoryFacet {

    private static final Class<? extends Facet> type() {
        return MandatoryFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private Semantics semantics;

    public MandatoryFacetAbstract(final FacetHolder holder, final Semantics semantics) {
        super(type(), holder);
        this.semantics = semantics;
    }

    public MandatoryFacetAbstract(
            final FacetHolder holder, final Semantics semantics, final Facet.Precedence precedence) {
        super(type(), holder, precedence);
        this.semantics = semantics;
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof MandatoryFacetAbstract
                ? this.getSemantics() == ((MandatoryFacetAbstract)other).getSemantics()
                : false;
    }

    /**
     * If not specified or, if a string, then zero length.
     */
    @Override
    public final boolean isRequiredButNull(final ManagedObject adapter) {
        if(getSemantics().isRequired()) {
            val pojo = MmUnwrapUtil.single(adapter);

            // special case string handling.
            if(pojo instanceof String) {
                return _Strings.isEmpty((String)pojo);
            }

            return pojo == null;
        } else {
            return false; // policy is not enforced
        }
    }

    @Override
    public String invalidates(final ValidityContext context) {

        val proposedHolder =
                context instanceof PropertyModifyContext
                || context instanceof ActionArgValidityContext
                        ? (ProposedHolder) context
                        : null;

        if(proposedHolder==null
                || !isRequiredButNull(proposedHolder.getProposed())) {
            return null;
        }

        return Optional.ofNullable(context.getFriendlyNameProvider())
        .map(Supplier::get)
        .filter(_Strings::isNotEmpty)
        .map(named->"'" + named + "' is mandatory")
        .orElse("Mandatory");
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("semantics", semantics);
    }
}
