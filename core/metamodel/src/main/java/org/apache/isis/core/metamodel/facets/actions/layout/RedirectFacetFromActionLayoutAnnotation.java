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

package org.apache.isis.core.metamodel.facets.actions.layout;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacet;
import org.apache.isis.core.metamodel.facets.actions.redirect.RedirectFacetAbstract;

public class RedirectFacetFromActionLayoutAnnotation extends RedirectFacetAbstract {

    public static RedirectFacet create(final ActionLayout actionLayout, final FacetHolder holder) {
        if(actionLayout == null) {
            return null;
        }
        final Redirect redirect = actionLayout.redirectPolicy();
        return redirect != null ? new RedirectFacetFromActionLayoutAnnotation(redirect, holder) : null;
    }

    public RedirectFacetFromActionLayoutAnnotation(
            final Redirect policy, final FacetHolder holder) {
        super(policy, holder);
    }

}
