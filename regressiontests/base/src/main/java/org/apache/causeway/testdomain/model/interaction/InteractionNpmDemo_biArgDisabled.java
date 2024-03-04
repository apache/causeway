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
import lombok.Value;
import lombok.experimental.Accessors;

@Action
@RequiredArgsConstructor
public class InteractionNpmDemo_biArgDisabled {

    @SuppressWarnings("unused")
    private final InteractionNpmDemo holder;

    @Value @Accessors(fluent = true)
    public static class Parameters {
        int a;
        int b;
    }

    @MemberSupport public int act(int a, int b) {
        return a + b;
    }

    @MemberSupport public boolean hide() {
        return false;
    }

    @MemberSupport public String disable() {
        return "Disabled for demonstration.";
    }

    @MemberSupport public String validate(Parameters params) {
        return "Never valid for demonstration.";
    }

    // -- PARAM SUPPORTING METHODS

    // testing whether all of these get picked up by the meta-model

    @MemberSupport public boolean hideA(Parameters params) { return false; }
    @MemberSupport public String disableA(Parameters params) { return null; }
    @MemberSupport public String validateA(Parameters params) { return null; }
    @MemberSupport public int[] choicesA(Parameters params) { return null; }
    @MemberSupport public int[] autoCompleteA(Parameters params, String search) { return null; }
    @MemberSupport public int defaultA(Parameters params) { return 0; }

    @MemberSupport public boolean hideB(Parameters params) { return false; }
    @MemberSupport public String disableB(Parameters params) { return null; }
    @MemberSupport public String validateB(Parameters params) { return null; }
    @MemberSupport public int[] choicesB(Parameters params) { return null; }
    @MemberSupport public int[] autoCompleteB(Parameters params, String search) { return null; }
    @MemberSupport public int defaultB(Parameters params) { return 0; }

}
