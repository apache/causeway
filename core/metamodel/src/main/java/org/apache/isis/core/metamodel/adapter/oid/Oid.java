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

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.version.Version;


/**
 * An immutable identifier for either a root object (subtype {@link RootOid}) or 
 * an aggregated object (subtype {@link AggregatedOid}).
 * 
 * <p>
 * Note that value objects (strings, ints, {@link Value}s etc) do not have an {@link Oid}. 
 */
public interface Oid extends Encodable {

    /**
     * A string representation of this {@link Oid}.
     */
    String enString(OidMarshaller oidMarshaller);

    String enStringNoVersion(OidMarshaller oidMarshaller);
    
    Version getVersion();
    void setVersion(Version version);

    /**
     * Flags whether this OID is for a transient (not-yet-persisted) object, 
     * or a view model object, or for a persistent object.
     * 
     * <p>
     * In the case of an {@link AggregatedOid}, is determined by the state 
     * of its {@link AggregatedOid#getParentOid() parent}'s {@link RootOid#isTransient() state}.
     */
    boolean isTransient();

    boolean isViewModel();
    
    boolean isPersistent();

    public static enum State {
        PERSISTENT("P"), 
        TRANSIENT("T"),
        VIEWMODEL("V");
        
        private final String code;
        private State(final String code) {
            this.code = code;
        }
        
        public boolean isTransient() {
            return this == TRANSIENT;
        }
        public boolean isViewModel() {
            return this == VIEWMODEL;
        }
        public boolean isPersistent() {
            return this == PERSISTENT;
        }

        public String getCode() {
            return code;
        }
    }


    public final static class Matchers {

        private Matchers() {
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

    
}
