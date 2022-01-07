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
package demoapp.dom.types.isis.markups.holder;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.value.Markup;

import lombok.RequiredArgsConstructor;

import demoapp.dom.types.Samples;

//tag::class[]
@Action(
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        promptStyle = PromptStyle.INLINE
        , named = "Update with choices"
        , associateWith = "readOnlyProperty"
        , sequence = "2")
@RequiredArgsConstructor
public class IsisMarkupHolder_updateReadOnlyPropertyWithChoices {

    private final IsisMarkupHolder holder;

    @MemberSupport public IsisMarkupHolder act(final Markup newValue) {
        holder.setReadOnlyProperty(newValue);
        return holder;
    }

    @MemberSupport public Markup default0Act() {
        return holder.getReadOnlyProperty();
    }

    @MemberSupport public List<Markup> choices0Act() {
        return samples.stream()
                .collect(Collectors.toList());
    }

    Samples<Markup> samples;
}
//end::class[]
