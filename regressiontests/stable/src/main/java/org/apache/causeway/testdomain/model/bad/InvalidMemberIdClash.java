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
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Action collisions on:
 * <ul>
 * <li>someAction: {act0, act1}</li>
 * <li>someProperty: none</li>
 * <li>someCollection: none</li>
 * <li>mixinA: none</li>
 * <li>mixinB: none</li>
 * <li>actionClash: {act6, act7}</li>
 * </ul>
 * Association collisions on:
 * <ul>
 * <li>someAction: {prop1, coll1}</li>
 * <li>someProperty: {prop0, prop2, coll2}</li>
 * <li>someCollection: {coll0, prop3, coll3}</li>
 * <li>mixinA: none</li>
 * <li>mixinB: none</li>
 * <li>mixinC: {prop5, coll5}</li>
 * <li>propertyClash: {prop6, prop7}</li>
 * <li>collectionClash: {prop6, prop7}</li>
 * </ul>
 * Expectations:
 * <ul>
 * <li>unique action-ids (total minus number of shadowed by collisions): 8 - 2 = 6</li>
 * <li>unique association-ids (total minus number of shadowed by collisions): 16 - 8 = 8</li>
 * </ul>
 */
@DomainObject(nature = Nature.VIEW_MODEL)
@Named("testdomain.InvalidMemberIdClash")
public class InvalidMemberIdClash {

    // member-id clash (act0)
    @Action
    public boolean someAction() {
        return false;
    }

    // member-id clash (prop0)
    @Property
    @Getter
    private int someProperty = 0;

    // member-id clash (coll0)
    @Collection
    @Getter
    private List<Integer> someCollection = Collections.emptyList();

    // -- MIXINS (ACTION)

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin1_someAction {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin2_someProperty {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin3_someCollection {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin4_mixinA {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin5_mixinB {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin6_actionClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    @Action
    @RequiredArgsConstructor
    public static class ActionMixin7_actionClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String act() { return ""; }
    }

    // -- MIXINS (PROPERTY)

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin1_someAction {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin2_someProperty {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin3_someCollection {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin_mixinA {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin5_mixinC {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin6_propertyClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    @Property
    @RequiredArgsConstructor
    public static class PropertyMixin7_propertyClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public String prop() { return ""; }
    }

    // -- MIXINS (COLLECTION)

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin1_someAction {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin2_someProperty {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin3_someCollection {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin4_mixinB {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin5_mixinC {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin6_collectionClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

    @Collection
    @RequiredArgsConstructor
    public static class CollectionMixin7_collectionClash {
        @SuppressWarnings("unused")
        private final InvalidMemberIdClash memberIdClash;
        @MemberSupport public Set<String> coll() { return null; }
    }

}
