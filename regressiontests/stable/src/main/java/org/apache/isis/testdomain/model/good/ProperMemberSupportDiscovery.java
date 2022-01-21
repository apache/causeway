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

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Introspection;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ProperMemberSupportDiscovery {

    public static abstract class PublicBase {

        // -- ACTION

        public String namedPlaceOrder() { return "my name"; }
        public String describedPlaceOrder() { return "my description"; }
        public boolean hidePlaceOrder() { return false; }
        public boolean hide0PlaceOrder(final String x) { return true; }
        public boolean hide1PlaceOrder(final String y) { return false; }
        public String disablePlaceOrder() { return "my disable reason"; }
        public String disable0PlaceOrder(final String x) { return "my disable reason-0";}
        public String disable1PlaceOrder(final String z) { return "my disable reason-1";}
        public String default0PlaceOrder() { return "my default-0";}
        public String default1PlaceOrder() { return "my default-1";}

        public java.util.Collection<String> choices0PlaceOrder(final String x) {
            return List.of("my choice");
        }
        public java.util.Collection<String> autoComplete1PlaceOrder(
                final String y,
                @MinLength(3) final String search) {
            return List.of("my search arg=" + search);
        }

        public String validate0PlaceOrder(final String x) { return "my validation-0";}
        public String validate1PlaceOrder(final String y) { return "my validation-1";}
        public String validatePlaceOrder(final String x, final String y) { return "my validation";}

        // -- PROPERTY

        public String namedEmail() { return "my email";}
        public String describedEmail() { return "my email described";}
        public boolean hideEmail() { return true;}
        public String disableEmail() { return "my email disable";}
        public String defaultEmail() { return "my default email";}
        public java.util.Collection<String> choicesEmail() {
            return List.of("my email choice");
        }
        public String validateEmail(final String email) { return "my email validate";}

        // -- COLLECTION

        public String namedOrders() { return "my orders"; }
        public String describedOrders() { return "my orders described"; }
        public boolean hideOrders() { return true;}
        public String disableOrders() { return "my orders disabled"; }

    }

    /**
     * annotations required, otherwise not picked up
     */
    static abstract class ProtectedBase {

        // -- ACTION

        protected abstract void placeOrder(String x, String y);

        @MemberSupport protected String namedPlaceOrder() { return "my name"; }
        @MemberSupport protected String describedPlaceOrder() { return "my description"; }
        @MemberSupport protected boolean hidePlaceOrder() { return false; }
        @MemberSupport protected boolean hide0PlaceOrder(final String x) { return true; }
        @MemberSupport protected boolean hide1PlaceOrder(final String y) { return false; }
        @MemberSupport protected String disablePlaceOrder() { return "my disable reason"; }
        @MemberSupport protected String disable0PlaceOrder(final String x) { return "my disable reason-0";}
        @MemberSupport protected String disable1PlaceOrder(final String z) { return "my disable reason-1";}
        @MemberSupport protected String default0PlaceOrder() { return "my default-0";}
        @MemberSupport protected String default1PlaceOrder() { return "my default-1";}

        @MemberSupport protected java.util.Collection<String> choices0PlaceOrder(final String x) {
            return List.of("my choice");
        }
        @MemberSupport protected java.util.Collection<String> autoComplete1PlaceOrder(
                final String y,
                @MinLength(3) final String search) {
            return List.of("my search");
        }

        // -- PROPERTY

        @MemberSupport protected String namedEmail() { return "my email";}
        @MemberSupport protected String describedEmail() { return "my email described";}
        @MemberSupport protected boolean hideEmail() { return true;}
        @MemberSupport protected String disableEmail() { return "my email disable";}
        @MemberSupport protected String defaultEmail() { return "my default email";}
        @MemberSupport protected java.util.Collection<String> choicesEmail() {
            return List.of("my email choice");
        }
        @MemberSupport protected String validateEmail(final String email) { return "my email validate";}

        // -- COLLECTION

        @MemberSupport protected String namedOrders() { return "my oders"; }
        @MemberSupport protected String describedOrders() { return "my orders described"; }
        @MemberSupport protected boolean hideOrders() { return true;}
        @MemberSupport protected String disableOrders() { return "my orders disabled"; }
    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_OPTIONAL)
    public static class WhenAnnotationOptional {

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
    extends PublicBase {

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
    extends ProtectedBase {

        // annotation required, otherwise not picked up as action
        @Action
        @Override
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

