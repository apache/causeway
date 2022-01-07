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
package demoapp.dom.types.javalang.doubles.holder;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.types.Samples;


//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class WrapperDoubleHolder_actionReturningCollection {

    private final WrapperDoubleHolder holder;

    public Collection<Double> act() {
        final Collection<Double> doubles = new ArrayList<>();
        val initial = holder.getReadOnlyProperty();
        samples.stream()
                .forEach(doubles::add);
        return doubles;
    }

    @Inject
    Samples<Double> samples;
}
//end::class[]
