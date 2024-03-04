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

import java.util.Collections;
import java.util.List;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

/**
 * For (test) mixin descriptions see {@link ProperMemberSupport}.
 */
@Collection
@CollectionLayout(named = "foo", describedAs = "bar")
@RequiredArgsConstructor
public class ProperMemberSupport_collection2 {

    private final ProperMemberSupport holder;

    //@Action(semantics=SAFE)   // <-- inferred (required)
    //@ActionLayout(contributed=ASSOCIATION)  // <-- inferred (required)

    @MemberSupport public List<String> coll() {
        return Collections.singletonList(holder.toString());
    }

    // -- PROPERLY DECLARED SUPPORTING METHODS

    @MemberSupport public boolean hideColl() {
        return false;
    }

    @MemberSupport public String disableColl() {
        return null;
    }

    //TODO not documented with the support-matrix, what to do here?
    @MemberSupport public String validateColl() {
        return null;
    }


}
