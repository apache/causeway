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
package demoapp.dom.domain.actions.ActionLayout.sequence;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@ActionLayout(
    associateWith = "name",
    sequence = "1",
    position = ActionLayout.Position.PANEL  // <.>
)
@RequiredArgsConstructor
public class ActionLayoutSequencePage_updateNamePositionedPanelSeqY {
    // ...
//end::class[]
    private final ActionLayoutSequencePage page;

    @MemberSupport public ActionLayoutSequencePage act(final String newValue) {
        page.setName(newValue);
        return page;
    }
    @MemberSupport public String default0Act() {
        return page.getName();
    }

//tag::class[]
}
//end::class[]
