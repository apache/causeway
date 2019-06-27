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

package org.apache.isis.metamodel.facets.members.disabled.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;

public class DisableForContextFacetViaMethod extends DisableForContextFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final TranslationService translationService;
    private final String translationContext;

    public DisableForContextFacetViaMethod(
            final Method method,
            final TranslationService translationService, final String translationContext,
            final FacetHolder holder) {
        super(holder);
        this.method = method;
        this.translationService = translationService;
        this.translationContext = translationContext;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHECK_IF_DISABLED;
    }

    /**
     * The reason this object is disabled, or <tt>null</tt> otherwise.
     */
    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        final ManagedObject target = ic.getTarget();
        if (target == null) {
            return null;
        }
        final Object returnValue = ObjectAdapter.InvokeUtils.invokeC(method, target, 
                _NullSafe.streamNullable(ic.getContributeeWithParamIndex()));
        if(returnValue instanceof String) {
            return (String) returnValue;
        }
        if(returnValue instanceof TranslatableString) {
            final TranslatableString ts = (TranslatableString) returnValue;
            return ts.translate(translationService, translationContext);
        }
        return null;
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
    }

}
