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
package demoapp.dom.types.wrapper.doubles.holder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;


//tag::class[]
@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        associateWith = "readOnlyProperty",
        associateWithSequence = "2"
)
@ActionLayout(promptStyle = PromptStyle.INLINE, named = "Update with choices")
@RequiredArgsConstructor
public class WrapperDoubleHolder_updateReadOnlyPropertyWithChoices {

    private final WrapperDoubleHolder holder;

    public WrapperDoubleHolder act(Double newValue) {
        holder.setReadOnlyProperty(newValue);
        return holder;
    }
    public Double default0Act() {
        return holder.getReadOnlyProperty();
    }
    public List<Double> choices0Act() {
        return Stream.of(123.4567, 762.1234, 9.00, -12.1314)
                .collect(Collectors.toList());
    }

}
//end::class[]
