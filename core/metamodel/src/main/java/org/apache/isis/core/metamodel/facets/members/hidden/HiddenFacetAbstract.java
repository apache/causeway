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

package org.apache.isis.core.metamodel.facets.members.hidden;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.WhenAndWhereValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;

public abstract class HiddenFacetAbstract extends WhenAndWhereValueFacetAbstract implements HiddenFacet {

    public static Class<? extends Facet> type() {
        return HiddenFacet.class;
    }

    public HiddenFacetAbstract(final When when, Where where, final FacetHolder holder) {
        this(type(), when, where, holder);
    }

    private HiddenFacetAbstract(final Class<? extends Facet> type, final When when, Where where, final FacetHolder holder) {
        super(type, holder, when, where);
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        return hiddenReason(ic.getTarget(), ic.getWhere());
    }

    /**
     * The reason why the (feature of the) target object is currently hidden, or
     * <tt>null</tt> if visible.
     */
    protected abstract String hiddenReason(ObjectAdapter target, Where whereContext);

}
