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
package demoapp.dom.domain.actions.Action.restrictTo;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.RestrictTo;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;


//tag::class[]
@ActionRestrictToNoRestrictionsMetaAnnotation   // <.>
@Action(
    semantics = SemanticsOf.SAFE
    , restrictTo = RestrictTo.PROTOTYPING       // <.>
)
@ActionLayout(
    describedAs =
        "@ActionRestrictToNoRestrictionsMetaAnnotation " +
        "@Action(restrictTo = RestrictTo.PROTOTYPING)"
    , associateWith = "propertyForMetaAnnotationsOverridden"
    , sequence = "2"
)
@RequiredArgsConstructor
public class ActionRestrictToVm_mixinUpdateMetaAnnotatedOverridden {

    private final ActionRestrictToVm actionRestrictToVm;

    @MemberSupport public ActionRestrictToVm act(final String text) {
        actionRestrictToVm.setPropertyForMetaAnnotationsOverridden(text);
        return actionRestrictToVm;
    }
    @MemberSupport public String default0Act() {
        return actionRestrictToVm.getPropertyForMetaAnnotationsOverridden();
    }
}
//end::class[]
