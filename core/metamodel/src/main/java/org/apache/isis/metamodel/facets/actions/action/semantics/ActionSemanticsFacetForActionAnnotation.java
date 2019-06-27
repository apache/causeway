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

package org.apache.isis.metamodel.facets.actions.action.semantics;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;

public class ActionSemanticsFacetForActionAnnotation extends ActionSemanticsFacetAbstract {

    public static ActionSemanticsFacet create(
            final List<Action> actions,
            final FacetHolder holder) {

        return actions.stream()
                .map(Action::semantics)
                .filter(semanticsOf -> semanticsOf != SemanticsOf.NOT_SPECIFIED)
                .findFirst()
                .map(semanticsOf ->
                (ActionSemanticsFacet)new ActionSemanticsFacetForActionAnnotation(semanticsOf, holder))
                .orElse(new ActionSemanticsFacetFallbackToNonIdempotent(holder));
    }

    private ActionSemanticsFacetForActionAnnotation(SemanticsOf of, final FacetHolder holder) {
        super(of, holder);
    }


}
