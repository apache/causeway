/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.testdomain.domainmodel.mixinapplicability;

import java.util.List;

import jakarta.inject.Named;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;

@Configuration
@ComponentScan(basePackageClasses = Configuration_usingMixinApplicability.class)
public class Configuration_usingMixinApplicability {
    // Deliberately not annotated with Domain.Include.
    public interface Capability { }

    @Named("prefilter.Base")
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Base { }

    @Named("prefilter.Subtype")
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Subtype extends Base implements Capability { }

    @Named("prefilter.Other")
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Other { }

    @Named("prefilter.Child")
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class Child { }

    @Property
    public static class Base_tag {
        public Base_tag(final Base mixee) { }
        @MemberSupport public String prop() { return "tag"; }
    }

    @Collection
    public static class Base_children {
        public Base_children(final Base mixee) { }
        @MemberSupport public List<Child> coll() { return List.of(); }
    }

    @Action
    public static class Capability_alpha {
        public Capability_alpha(final Capability mixee) { }
        @ActionLayout(sequence = "20")
        @MemberSupport public Child act() { return null; }
    }

    @Action
    public static class Capability_zulu {
        public Capability_zulu(final Capability mixee) { }
        @ActionLayout(sequence = "10")
        @MemberSupport public void act() { }
    }

    @Property
    public static class Other_foreignTag {
        public Other_foreignTag(final Other mixee) { }
        @MemberSupport public String prop() { return "other"; }
    }

    @Collection
    public static class Other_foreignChildren {
        public Other_foreignChildren(final Other mixee) { }
        @MemberSupport public List<Child> coll() { return List.of(); }
    }

    @Action
    public static class Other_foreignAction {
        public Other_foreignAction(final Other mixee) { }
        @MemberSupport public void act() { }
    }
}
