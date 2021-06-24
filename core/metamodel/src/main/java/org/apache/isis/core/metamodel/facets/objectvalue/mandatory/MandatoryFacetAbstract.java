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

package org.apache.isis.core.metamodel.facets.objectvalue.mandatory;

import java.util.function.BiConsumer;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;

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
            val pojo = UnwrapUtil.single(adapter);

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
        if (!(context instanceof PropertyModifyContext)
                && !(context instanceof ActionArgValidityContext)) {
            return null;
        }
        // TODO: IntelliJ says the following is always false, so looks like it can be removed...
        if (!(context instanceof ProposedHolder)) {
            // shouldn't happen, since both the above should hold a proposed
            // value/argument
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) context;
        final boolean required = isRequiredButNull(proposedHolder.getProposed());
        if (!required) {
            return null;
        }
        val name = getFacetHolder().lookupFacet(NamedFacet.class)
        .map(NamedFacet::getSpecialization)
        .map(specialization->specialization
                .fold(textFacet->textFacet.preferredTranslated(),
                      textFacet->textFacet.textElseNull(context.getHead().getTarget())))
        .orElse(null);

        return name != null
                ? "'" + name + "' is mandatory"
                : "Mandatory";
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("semantics", semantics);
    }
}
