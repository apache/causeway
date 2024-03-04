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
package org.apache.causeway.testdomain.model.good;

import java.util.List;
import java.util.Set;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.Publishing;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * For (test) mixin descriptions see {@link ProperMemberSupport}.
 */
@Action(executionPublishing = Publishing.ENABLED)
@RequiredArgsConstructor
public class ProperMemberSupport_action4 {

    private final ProperMemberSupport holder;

    @Value @Accessors(fluent = true)
    public static class Parameter {
        List<String> a;
        List<String> b;
    }

    // proper mix-in action

    public ProperMemberSupport act(final List<String> a, final List<String> b) {
        return holder;
    }

    @MemberSupport public String disableAct() {
        return null;
    }

    @MemberSupport public boolean hideAct() {
        return false;
    }

    @MemberSupport public String validateAct(final Parameter params) {
        return null;
    }

    @MemberSupport public Set<String> autoCompleteA(final Parameter params, @MinLength(3) final String search) {
        return null;
    }

    @MemberSupport public Set<String> autoCompleteB(final Parameter params, @MinLength(3) final String search) {
        return null;
    }

    @MemberSupport public Set<String> choicesA(final Parameter params) {
        return null;
    }

    @MemberSupport public Set<String> choicesB(final Parameter params) {
        return null;
    }

    @MemberSupport public List<String> defaultA(final Parameter params) {
        return null;
    }

    @MemberSupport public List<String> defaultB(final Parameter params) {
        return null;
    }

    @MemberSupport public String validateA(final Parameter params) {
        return null;
    }

    @MemberSupport public String validateB(final Parameter params) {
        return null;
    }


}
