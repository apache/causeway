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
package org.apache.causeway.core.metamodel.facets.members.hidden.method;

import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

public record HiddenFacetForMemberViaMethod(
		Can<MethodFacade> methods,
		FacetHolder facetHolder
		) implements HiddenFacetForMember, ImperativeFacet {
	
	@Override public Class<? extends Facet> facetType() { return HiddenFacetForMember.class; }
	@Override public Precedence precedence() { return Precedence.DEFAULT; }
	@Override public Intent intent() { return Intent.CHECK_IF_HIDDEN;}

    public Can<MethodFacade> getMethods() { return methods(); }

    public HiddenFacetForMemberViaMethod(final ResolvedMethod method, final FacetHolder holder) {
        this(ImperativeFacet.singleRegularMethod(method), holder);
    }

    @Override
    public String hides(final VisibilityContext ic) {
        final ManagedObject target = ic.target();
        if (target == null) return null;

        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        final Boolean isHidden = (Boolean) MmInvokeUtils.invokeAutofit(method.method(), target);
        return isHidden.booleanValue() ? "Hidden" : null;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	HiddenFacetForMember.super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }
    
    @Override
    public String toString() {
        return FacetUtil.toString(this);
    }

}
