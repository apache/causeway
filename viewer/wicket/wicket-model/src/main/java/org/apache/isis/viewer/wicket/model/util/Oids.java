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

package org.apache.isis.viewer.wicket.model.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import org.apache.isis.core.metamodel.adapter.oid.Oid;

public final class Oids {

    private Oids() {
    }

    public static Matcher<Oid> isTransient() {
        return new TypeSafeMatcher<Oid>() {

            @Override
            public boolean matchesSafely(final Oid item) {
                return item.isTransient();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("is transient");
            }
        };
    }

    public static Matcher<Oid> isPersistent() {
        return new TypeSafeMatcher<Oid>() {

            @Override
            public boolean matchesSafely(final Oid item) {
                return !item.isTransient();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("is persistent");
            }
        };
    }

}
