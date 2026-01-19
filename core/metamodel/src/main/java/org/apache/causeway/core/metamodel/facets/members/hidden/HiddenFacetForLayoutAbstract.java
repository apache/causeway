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
package org.apache.causeway.core.metamodel.facets.members.hidden;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.WhereValueFacetAbstract;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacetForLayout;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public abstract class HiddenFacetForLayoutAbstract
extends WhereValueFacetAbstract
implements HiddenFacetForLayout {

    public static final Class<HiddenFacetForLayout> type() {
        return HiddenFacetForLayout.class;
    }

    public HiddenFacetForLayoutAbstract(
            final Where where,
            final FacetHolder holder) {
        super(type(), holder, where);
    }

    public HiddenFacetForLayoutAbstract(
            final Where where,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, where, precedence);
    }

    // to instantiate contributed facets
    private HiddenFacetForLayoutAbstract(final HiddenFacetForLayoutAbstract toplevelFacet) {
        super(type(), toplevelFacet.facetHolder(), toplevelFacet.where());
    }

    @Override
    public final String hides(final VisibilityContext ic) {
        return hiddenReason(ic.target(), ic.where());
    }

    /**
     * The reason why the (feature of the) target object is currently hidden, or
     * <tt>null</tt> if visible.
     */
    protected abstract String hiddenReason(ManagedObject target, Where whereContext);

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
    }

}
