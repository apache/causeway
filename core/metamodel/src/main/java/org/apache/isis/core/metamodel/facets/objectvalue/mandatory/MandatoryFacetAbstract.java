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

import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.MarkerFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public abstract class MandatoryFacetAbstract extends MarkerFacetAbstract implements MandatoryFacet {

    public static Class<? extends Facet> type() {
        return MandatoryFacet.class;
    }

    public enum Semantics {
        REQUIRED,
        OPTIONAL;

        public static Semantics of(boolean required) {
            return required ? REQUIRED: OPTIONAL;
        }
    }

    private Semantics semantics;

    public MandatoryFacetAbstract(final FacetHolder holder, final Semantics semantics) {
        super(type(), holder);
        this.semantics = semantics;
    }

    /**
     * If not specified or, if a string, then zero length.
     */
    @Override
    public final boolean isRequiredButNull(final ManagedObject adapter) {
        if(!isInvertedSemantics()) {
            final Object object = ObjectAdapter.Util.unwrap(adapter);
            if (object == null) {
                return true;
            }
            // special case string handling.
            final String str = ObjectAdapter.Util.unwrapAsString(adapter);
            return str != null && str.length() == 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean isInvertedSemantics() {
        return this.semantics == Semantics.OPTIONAL;
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> context) {
        if (!(context instanceof PropertyModifyContext) && !(context instanceof ActionArgValidityContext)) {
            return null;
        }
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
        final NamedFacet namedFacet = getFacetHolder().getFacet(NamedFacet.class);
        final String name = namedFacet != null? namedFacet.value(): null;
        return name != null? "'" + name + "' is mandatory":"Mandatory";
    }
}
