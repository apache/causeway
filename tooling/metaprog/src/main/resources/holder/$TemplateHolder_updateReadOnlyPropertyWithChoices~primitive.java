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
package /*${java-package}*/;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;

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
public class /*${showcase-name}*/Holder_updateReadOnlyPropertyWithChoices {

    private final /*${showcase-name}*/Holder holder;

    @MemberSupport public /*${showcase-name}*/Holder act(final /*${showcase-simple-type}*/ newValue) {
        holder.setReadOnlyProperty(newValue);
        return holder;
    }

    @MemberSupport public /*${showcase-simple-type}*/ default0Act() {
        return holder./*${showcase-simple-type-getter-prefix}*/ReadOnlyProperty();
    }

    @MemberSupport public List</*${showcase-simple-type-boxed}*/> choices0Act() {
        return samples.stream()
                .collect(Collectors.toList());
    }

    @Inject
    Samples</*${showcase-simple-type-boxed}*/> samples;

}
//end::class[]
