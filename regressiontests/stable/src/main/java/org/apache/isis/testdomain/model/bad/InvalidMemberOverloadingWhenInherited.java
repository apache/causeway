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
package org.apache.isis.testdomain.model.bad;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Introspection;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;

@DomainObject(nature = Nature.VIEW_MODEL)
public class InvalidMemberOverloadingWhenInherited {

    static abstract class Base {

        // overloading not allowed, unless programmatic
        public String anAction(final String param) {
            return param;
        }

        // boolean property name clash, overloading not allowed even though programmatic
        @Programmatic
        public boolean isActive() {
            return true;
        }

    }


    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_OPTIONAL)
    public static class WhenAnnotationOptional
    extends Base {

        // overloading not allowed: should fail
        public String anAction() {
            return "";
        }

        // name-clash with base property
        @Getter @Setter
        private Boolean active;

    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ANNOTATION_REQUIRED)
    public static class WhenAnnotationRequired
    extends Base {

        // overloading not allowed: should fail
        @Action
        public String anAction() {
            return "";
        }

        // name-clash with base property
        @Property
        @Getter @Setter
        private Boolean active;

    }

    @DomainObject(
            nature = Nature.VIEW_MODEL,
            introspection = Introspection.ENCAPSULATION_ENABLED)
    public static class WhenEncapsulationEnabled
    extends Base {

        // overloading not allowed: should fail
        @Action
        protected String anAction() {
            return "";
        }

        // name-clash with base property
        @Property
        private Boolean active;
        protected Boolean getActive() { return null;}
        protected void setActive(final Boolean flag) { }

    }

}

