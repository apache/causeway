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
package demoapp.dom.types.primitive.longs.holder;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;


@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        associateWith = "readOnlyProperty",
        associateWithSequence = "3"
)
@ActionLayout(promptStyle = PromptStyle.DIALOG_SIDEBAR, named = "sidebar", describedAs = "promptStyle = DIALOG_SIDEBAR")
@RequiredArgsConstructor
public class PrimitiveLongHolder_updateReadOnlyPropertyPromptStyleSidebar {

    private final PrimitiveLongHolder primitiveLongHolder;

    public PrimitiveLongHolder act(long newValue) {
        primitiveLongHolder.setReadOnlyProperty(newValue);
        return primitiveLongHolder;
    }
    public long default0Act() {
        return primitiveLongHolder.getReadOnlyProperty();
    }


}
