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

import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.RequiredArgsConstructor;

/**
 * For (test) mixin descriptions see {@link ProperMemberSupport}.
 */
@Property
@PropertyLayout(named= "foo", describedAs = "bar") // <-- overruled by imperative naming, but used for column naming and describing
@RequiredArgsConstructor
public class ProperMemberSupport_property3 {

    private final ProperMemberSupport holder;

    @MemberSupport public String prop() {
        return holder.toString();
    }

    // -- IMPERATIVE NAMING AND DESCRIBING

    @MemberSupport public String namedProp() {
        return "named-imperative[property3]";
    }

    @MemberSupport public String describedProp() {
        return "described-imperative[property3]";
    }


}
