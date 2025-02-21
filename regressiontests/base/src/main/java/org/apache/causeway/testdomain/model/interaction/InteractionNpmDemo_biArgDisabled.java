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

import lombok.RequiredArgsConstructor;

@Action
@RequiredArgsConstructor
public class InteractionNpmDemo_biArgDisabled {

    @SuppressWarnings("unused")
    private final InteractionNpmDemo holder;

    public record Parameters(int a, int b) {}

    @MemberSupport public int act(final int a, final int b) {
        return a + b;
    }

    @MemberSupport public boolean hide() {
        return false;
    }

    @MemberSupport public String disable() {
        return "Disabled for demonstration.";
    }

    @MemberSupport public String validate(final Parameters params) {
        return "Never valid for demonstration.";
    }

    // -- PARAM SUPPORTING METHODS

    // testing whether all of these get picked up by the meta-model

    @MemberSupport public boolean hideA(final Parameters params) { return false; }
    @MemberSupport public String disableA(final Parameters params) { return null; }
    @MemberSupport public String validateA(final Parameters params) { return null; }
    @MemberSupport public int[] choicesA(final Parameters params) { return null; }
    @MemberSupport public int[] autoCompleteA(final Parameters params, final String search) { return null; }
    @MemberSupport public int defaultA(final Parameters params) { return 0; }

    @MemberSupport public boolean hideB(final Parameters params) { return false; }
    @MemberSupport public String disableB(final Parameters params) { return null; }
    @MemberSupport public String validateB(final Parameters params) { return null; }
    @MemberSupport public int[] choicesB(final Parameters params) { return null; }
    @MemberSupport public int[] autoCompleteB(final Parameters params, final String search) { return null; }
    @MemberSupport public int defaultB(final Parameters params) { return 0; }

}
