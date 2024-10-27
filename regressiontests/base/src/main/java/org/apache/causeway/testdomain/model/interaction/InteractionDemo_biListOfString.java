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
package org.apache.causeway.testdomain.model.interaction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

@Action
@RequiredArgsConstructor
public class InteractionDemo_biListOfString {

    @SuppressWarnings("unused")
    private final InteractionDemo holder;

    @MemberSupport public String act(final List<String> a, final List<String> b) {
        return Stream.concat(a.stream(), b.stream())
                .collect(Collectors.joining(","));
    }

    // -- PARAM A

    @MemberSupport public List<String> defaultA(final ParameterSamples.BiListOfString p) {
        return List.of("a1", "a2", "a3");
    }

    @MemberSupport public List<String> choicesA(final ParameterSamples.BiListOfString p) {
        return List.of("a1", "a2", "a3", "a4");
    }

    // -- PARAM B

    @MemberSupport public List<String> defaultB(final ParameterSamples.BiListOfString p) {
        return List.of("b1");
    }

    @MemberSupport public List<String> choicesB(final ParameterSamples.BiListOfString p) {
        return List.of("b1", "b2", "b3", "b4");
    }

}
