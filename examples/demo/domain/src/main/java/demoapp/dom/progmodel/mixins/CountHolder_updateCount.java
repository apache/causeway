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
package demoapp.dom.progmodel.mixins;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Action                                 // <.>
@ActionLayout(associateWith = "count")
@RequiredArgsConstructor                // <.>
public class CountHolder_updateCount {

    private final CountHolder holder;   // <.>

    @MemberSupport public CountHolder act(final int count) {
        holder.setCount(count);
        return holder;
    }
    @MemberSupport public int default0Act() {
        return holder.getCount();
    }
    @MemberSupport public String validate0Act(final int proposedCount) {
        return proposedCount >= 0 && proposedCount <= 46
                ? null
                : "Must be in the range [0,46]";
    }
}
//end::class[]
