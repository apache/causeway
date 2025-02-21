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

import java.util.Collection;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;

@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperParameterSupport {

    record Params(
            String p0,
            int p1,
            boolean p2) {
    }

    @Action
    public void act(
            final String p0,
            final int p1,
            final boolean p2) {
    }

    // -- PARAM 0 (String)

    @MemberSupport public String default0Act(final Params p) {
        return null;
    }

    @MemberSupport public Collection<String> choices0Act(final Params p) {
        return null;
    }

    @MemberSupport public Collection<String> autoComplete0Act(final Params p, final String search) {
        return null;
    }

    @MemberSupport public boolean hide0Act(final Params p) {
        return false;
    }

    @MemberSupport public String disable0Act(final Params p) {
        return null;
    }

    // -- PARAM 1 (int)

    @MemberSupport public int default1Act(final Params p) {
        return 0;
    }

    @MemberSupport public int[] choices1Act(final Params p) {
        return null;
    }

    @MemberSupport public int[] autoComplete1Act(final Params p, final String search) {
        return null;
    }

    @MemberSupport public boolean hide1Act(final Params p) {
        return false;
    }

    @MemberSupport public String disable1Act(final Params p) {
        return null;
    }

    // -- PARAM 2 (boolean)

    @MemberSupport public boolean default2Act(final Params p) {
        return false;
    }

    @MemberSupport public boolean[] choices2Act(final Params p) {
        return null;
    }

    @MemberSupport public boolean[] autoComplete2Act(final Params p, final String search) {
        return null;
    }

    @MemberSupport public boolean hide2Act(final Params p) {
        return false;
    }

    @MemberSupport public String disable2Act(final Params p) {
        return null;
    }

}
