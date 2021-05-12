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
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
public class AmbiguousMixinAnnotations {

    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Mixee {

    }

    // -- SHOULD FAIL VALIDATION

    @Property
    @RequiredArgsConstructor
    public static class InvalidMixinP {
        private final Mixee mixee;

        @Property
        public String prop() {
            return null;
        }
    }

    @PropertyLayout
    @RequiredArgsConstructor
    public static class InvalidMixinPL {
        private final Mixee mixee;

        @PropertyLayout
        public String prop() {
            return null;
        }
    }

    @Collection
    @RequiredArgsConstructor
    public static class InvalidMixinC {
        private final Mixee mixee;

        @Collection
        public String coll() {
            return null;
        }
    }

    @CollectionLayout
    @RequiredArgsConstructor
    public static class InvalidMixinCL {
        private final Mixee mixee;

        @CollectionLayout
        public String coll() {
            return null;
        }
    }

    @Action
    @RequiredArgsConstructor
    public static class InvalidMixinA {
        private final Mixee mixee;

        @Action
        public String act() {
            return null;
        }
    }

    @ActionLayout
    @RequiredArgsConstructor
    public static class InvalidMixinAL {
        private final Mixee mixee;

        @ActionLayout
        public String act() {
            return null;
        }
    }


}
