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
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;

import lombok.Getter;
import lombok.Setter;

abstract class ProperMemberInheritanceAbstract {

    @Title
    public String title() {
        return "inherited title";
    }

    @ObjectSupport
    public String iconName() {
        return "inherited icon";
    }

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    public void sampleAction() {
    }

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    @Getter @Setter
    private String sampleProperty;

    @Collection
    @CollectionLayout(named = "foo", describedAs = "bar")
    @Getter @Setter
    private List<String> sampleCollection;

    // -- OVERRIDING TESTS

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    public void sampleActionOverride() {
    }

    //FIXME[ISIS-2774] method overloading is not allowed - MM validation must fail, but currently does not?
    // move this to the 'bad' domain and check for validation failures
//    @Action
//    @ActionLayout(named = "foo", describedAs = "bar")
//    public void sampleActionOverride(String x) {
//    }

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    @Getter @Setter
    private String samplePropertyOverride;

}
