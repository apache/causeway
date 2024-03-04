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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.ParameterTuple;

import lombok.RequiredArgsConstructor;

@Action
@RequiredArgsConstructor
public class InteractionNpmDemo_patEnabled {

    @SuppressWarnings("unused")
    private final InteractionNpmDemo holder;

    /**
     * Parameters as Tuple (PAT) example. 
     * However, using Java records is the recommended way, once available.
     */
    public static class Params {
        private final int a;
        private final int b;

        public Params(
                @Parameter(maxLength = 2) // setup so we can test for this facet
                @ParameterLayout(describedAs = "first") // setup so we can test for this facet
                final int a,
                final int b) {
            this.a = a;
            this.b = b;
        }
    }

    @MemberSupport public int act(final @ParameterTuple Params p) {
        return p.a + p.b;
    }

    // -- PARAM 0

    // [CAUSEWAY-2362] parameter supporting methods, to be referenced by param name
    @MemberSupport public int defaultA(Params params) {
        return 5;
    }

    // -- PARAM 1

    // [CAUSEWAY-2362] parameter supporting methods, to be referenced by param name
    @MemberSupport public int[] choicesB(Params params) {
        return new int[] {1, 2, 3, 4};
    }
}
