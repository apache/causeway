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
package org.apache.causeway.testdomain.model.bad;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Collisions between:
 * <ul>
 * <li>act<->act1,prop1,coll1</li>
 * <li>prop<->act2,prop2,coll2</li>
 * <li>coll<->act3,prop3,coll3</li>
 * <li>act4<->prop4</li>
 * <li>act5<->coll4</li>
 * <li>prop5<->coll5</li>
 * </ul>
 */
@DomainObject(nature = Nature.VIEW_MODEL)
@Named("testdomain.InvalidMemberIdClash")
public class InvalidMemberIdClash {

    // member-id clash (act)
    @Action
    public boolean someAction() {
        return false;
    }

    // member-id clash (prop)
    @Property
    @Getter
    private int someProperty = 0;

    // member-id clash (coll)
    @Collection
    @Getter
    private List<Integer> someCollection = Collections.emptyList();

    // -- MIXINS

    @Action
    @ActionLayout(named = "someAction") // member-id clash
    @RequiredArgsConstructor
    public static class ActionMixin1 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @ActionLayout(named = "someProperty") // member-id clash
    @RequiredArgsConstructor
    public static class ActionMixin2 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @ActionLayout(named = "someCollection") // member-id clash
    @RequiredArgsConstructor
    public static class ActionMixin3 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @ActionLayout(named = "mixinA") // member-id clash
    @RequiredArgsConstructor
    public static class ActionMixin4 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @ActionLayout(named = "mixinB") // member-id clash
    @RequiredArgsConstructor
    public static class ActionMixin5 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Property
    @PropertyLayout(named = "someAction") // member-id clash
    @RequiredArgsConstructor
    public static class PropertyMixin1 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @PropertyLayout(named = "someProperty") // member-id clash
    @RequiredArgsConstructor
    public static class PropertyMixin2 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @PropertyLayout(named = "someCollection") // member-id clash
    @RequiredArgsConstructor
    public static class PropertyMixin3 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @PropertyLayout(named = "mixinA") // member-id clash
    @RequiredArgsConstructor
    public static class PropertyMixin4 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @PropertyLayout(named = "mixinC") // member-id clash
    @RequiredArgsConstructor
    public static class PropertyMixin5 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Collection
    @CollectionLayout(named = "someAction") // member-id clash
    @RequiredArgsConstructor
    public static class CollectionMixin1 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @CollectionLayout(named = "someProperty") // member-id clash
    @RequiredArgsConstructor
    public static class CollectionMixin2 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @CollectionLayout(named = "someCollection") // member-id clash
    @RequiredArgsConstructor
    public static class CollectionMixin3 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @CollectionLayout(named = "mixinB") // member-id clash
    @RequiredArgsConstructor
    public static class CollectionMixin4 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @CollectionLayout(named = "mixinC") // member-id clash
    @RequiredArgsConstructor
    public static class CollectionMixin5 {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

}
