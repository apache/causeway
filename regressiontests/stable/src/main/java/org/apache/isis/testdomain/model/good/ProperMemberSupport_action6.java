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
package org.apache.isis.testdomain.model.good;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

/**
 * Testing annotation provided 'choicesFrom' fallback,
 * if no explicit choices member-support is given.
 * <p>
 * For (test) mixin descriptions see {@link ProperMemberSupport}.
 */
@Action(choicesFrom = "myColl")
@RequiredArgsConstructor
public class ProperMemberSupport_action6 {

    private final ProperMemberSupport mixee;

    @MemberSupport public ProperMemberSupport act(
            final String p0, final String p1) {
        return mixee;
    }

}
