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

package org.apache.isis.core.metamodel.facets.actions.action.semantics;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;

public class ActionSemanticsFacetForActionAnnotation extends ActionSemanticsFacetAbstract {

    public static ActionSemanticsFacet create(
            final Action action,
            final FacetHolder holder) {

        if(action == null) {
            return null;
        }

        final SemanticsOf semantics = action.semantics();
        if(action.semantics() == null) {
            // don't think this can happen, therefore will return a facet with the default, ie NON_IDEMPOTENT
            return null;
        }

        return new ActionSemanticsFacetForActionAnnotation(
                SemanticsOf.from(semantics), holder);
    }

    private ActionSemanticsFacetForActionAnnotation(Of of, final FacetHolder holder) {
        super(of, holder);
    }


}
