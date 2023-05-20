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
package demoapp.dom.types.javasql.javasqldate.holder;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;

import java.sql.Date;

import lombok.RequiredArgsConstructor;

import demoapp.dom.types.Samples;

//tag::class[]
@Action(
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        promptStyle = PromptStyle.INLINE
        , named = "Update with choices"
        , associateWith = "readOnlyOptionalProperty"
        , sequence = "2")
@RequiredArgsConstructor
public class JavaSqlDateHolder_updateReadOnlyOptionalPropertyWithChoices {

    private final JavaSqlDateHolder holder;

    @MemberSupport public JavaSqlDateHolder act(
            @Parameter(optionality = Optionality.OPTIONAL)
            final Date newValue) {
        holder.setReadOnlyOptionalProperty(newValue);
        return holder;
    }

    @MemberSupport public Date default0Act() {
        return holder.getReadOnlyOptionalProperty();
    }

    @MemberSupport public List<Date> choices0Act() {
        return samples.stream()
                .collect(Collectors.toList());
    }

    @Inject
    Samples<Date> samples;
}
//end::class[]
