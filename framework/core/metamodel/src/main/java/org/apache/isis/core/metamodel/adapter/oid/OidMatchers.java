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
package org.apache.isis.core.metamodel.adapter.oid;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OidMatchers {
    
    private OidMatchers(){}

    public static Matcher<Oid> matching(final String objectType, final String identifier) {
        return new TypeSafeMatcher<Oid>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("matching [" + objectType + ", " + identifier +"]");
            }

            @Override
            public boolean matchesSafely(Oid oid) {
                if(oid instanceof RootOid) {
                    RootOid rootOid = (RootOid) oid;
                    return rootOid.getObjectSpecId().equals(objectType) && rootOid.getIdentifier().equals(identifier);
                }
                return false;
            }
        };
    }

}
