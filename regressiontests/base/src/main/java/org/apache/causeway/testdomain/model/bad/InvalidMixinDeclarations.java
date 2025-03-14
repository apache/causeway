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

import java.util.List;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.RequiredArgsConstructor;

@DomainObject(nature = Nature.VIEW_MODEL, introspection = Introspection.ANNOTATION_REQUIRED)
@Named("testdomain.InvalidMixinDeclarations")
public class InvalidMixinDeclarations {

    // -- INVALID SCENARIOS

    @Action
    @ActionLayout(named = "someActionProp")
    @RequiredArgsConstructor
    public static class ActionMixinWithProp {
        @SuppressWarnings("unused")
        private final InvalidMixinDeclarations mixee;
        @MemberSupport public String other() { return ""; }
    }

    @Action
    @ActionLayout(named = "someActionColl")
    @RequiredArgsConstructor
    public static class ActionMixinWithColl {
        @SuppressWarnings("unused")
        private final InvalidMixinDeclarations mixee;
        @MemberSupport public List<String> other() { return List.of(); }
    }

    @Property
    @PropertyLayout(named = "someProperty")
    @RequiredArgsConstructor
    public static class PropertyMixinWithOther {
        @SuppressWarnings("unused")
        private final InvalidMixinDeclarations mixee;
        @MemberSupport public void other() { }
    }

    @Collection
    @CollectionLayout(named = "someCollection")
    @RequiredArgsConstructor
    public static class CollectionMixinWithOther {
        @SuppressWarnings("unused")
        private final InvalidMixinDeclarations mixee;
        @MemberSupport public void other() { }
    }

}
