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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.testdomain.model.base.MemberDetection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ProperMemberSupportDiscovery {

     @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_OPTIONAL)
    public static class WhenAnnotationOptional
    extends MemberDetection.PublicBase {

        // no annotation required, should be picked up as action
        public void placeOrder(final String x, final String y) {
        }

        @Getter @Setter
        private String email;

        @Getter @Setter
        private java.util.Collection<String> orders;

    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_REQUIRED)
    public static class WhenAnnotationRequired
    extends MemberDetection.PublicBase {

        // annotation required, otherwise not picked up as action
        @Action
        public void placeOrder(final String x, final String y) {
        }

        @Property
        @Getter @Setter
        private String email;

        @Collection
        @Getter @Setter
        private java.util.Collection<String> orders;

    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ENCAPSULATION_ENABLED)
    public static class WhenEncapsulationEnabled
    extends MemberDetection.ProtectedBase {

        // annotation required, otherwise not picked up as action
        @Action
        //@Override
        protected void placeOrder(final String x, final String y) {
        }

        @Property
        @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
        private String email;

        @Collection
        @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
        private java.util.Collection<String> orders;

    }


}

