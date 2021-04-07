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
package demoapp.dom.domain.actions.Action.semantics;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;


//tag::class[]
@ActionSemanticsIdempotentMetaAnnotation  // <.>
@Action(
    semantics = SemanticsOf.SAFE
    , associateWith = "propertyForMetaAnnotations"
)
@ActionLayout(
    named = "Set to Value (Mixin)"
    , describedAs = "@ActionSemanticsIdempotentMetaAnnotation"
    , sequence = "2"
)
@RequiredArgsConstructor
public class ActionSemanticsVm_mixinSetToValueForPropertyMetaAnnotated {

    private final ActionSemanticsVm actionSemanticsVm;

    public ActionSemanticsVm act(final int value) {
        actionSemanticsVm.setPropertyForMetaAnnotations(value);
        return actionSemanticsVm;
    }
    public int default0Act() {
        return actionSemanticsVm.getPropertyForMetaAnnotations();
    }
}
//end::class[]
