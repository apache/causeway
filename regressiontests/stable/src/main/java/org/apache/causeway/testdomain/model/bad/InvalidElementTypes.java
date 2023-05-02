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

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@DomainObject(nature = Nature.VIEW_MODEL, introspection = Introspection.ANNOTATION_REQUIRED)
@Named("testdomain.InvalidElementTypes")
public class InvalidElementTypes {

    // -- SAMPLE MIXIN

    @Action
    @ActionLayout(named = "someAction")
    @RequiredArgsConstructor
    public static class ActionMixin {
        @SuppressWarnings("unused")
        private final InvalidElementTypes invalidElementTypes;
        @MemberSupport public String act() { return ""; }
    }

    // -- INVALID SCENARIOS

    @DomainObject(nature = Nature.VIEW_MODEL)
    @Named("testdomain.InvalidElementTypes.Returning")
    public static class Returning {
        // invalid return
        @Action
        public ActionMixin returningMixin() {
            return null;
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    @Named("testdomain.InvalidElementTypes.Taking")
    public static class Taking {
        // invalid param
        @Action
        public String takingMixin(final ActionMixin p0) {
            return null;
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    @Named("testdomain.InvalidElementTypes.Property")
    public static class InvalidProperty {
        // invalid property type
        @Property
        @Getter
        private ActionMixin propertyOfMixin = null;
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    @Named("testdomain.InvalidElementTypes.Collection")
    public static class InvalidCollection {
        // invalid element type
        @Collection
        @Getter
        private List<ActionMixin> collectionOfMixin = Collections.emptyList();
    }

}
