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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.WhereValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * This once implemented {@code org.apache.isis.core.metamodel.facetapi.MultiTypedFacet} 
 * so that each concrete implementation
 * is added to the eventual {@link FacetHolder} twice: 
 * once under <tt>HiddeFacet.class</tt> and 
 * once under its own concrete type class (eg <tt>HiddenFacetForActionAnnotation</tt>).
 * This satisfies a couple of (independent) requirements:
 * <ul>
 *     <li>that we don't have the concept of a single (blessed?) HiddenFacet; rather there are simply facets some of
 *     which implement {@link HidingInteractionAdvisor}</li>
 *     <li>that there is nevertheless always at least one facet that is registered under <tt>HiddenFacet.class</tt>;
 *     this was once used by the 
 *     {@code org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson} exporter</li>
 * </ul>
 */
public abstract class HiddenFacetAbstract 
extends WhereValueFacetAbstract 
implements HiddenFacet {

    public HiddenFacetAbstract(
            Class<? extends Facet> facetType,
            Where where,
            FacetHolder holder) {
        
        super(facetType, holder, where);
        super.setFacetAliasType(HiddenFacet.class);
    }
    
    // to instantiate contributed facets
    private HiddenFacetAbstract(HiddenFacetAbstract toplevelFacet) {
        super(HiddenFacet.class, toplevelFacet.getFacetHolder(), toplevelFacet.where());
    }

    /**
     * For testing only.
     */
    public HiddenFacetAbstract(Where where, FacetHolder holder) {
        super(HiddenFacetAbstract.class, holder, where);
    }

    @Override
    public String hides(VisibilityContext ic) {
        return hiddenReason(ic.getTarget(), ic.getWhere());
    }

    /**
     * The reason why the (feature of the) target object is currently hidden, or
     * <tt>null</tt> if visible.
     */
    protected abstract String hiddenReason(ManagedObject target, Where whereContext);
    
}
