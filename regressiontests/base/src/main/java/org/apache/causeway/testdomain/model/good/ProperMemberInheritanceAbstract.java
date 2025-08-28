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

import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.Setter;

abstract class ProperMemberInheritanceAbstract {

    @ObjectSupport public String title() {
        return "inherited title";
    }

    @ObjectSupport public ObjectSupport.IconResource icon(final ObjectSupport.IconWhere iconWhere) {
        return new ObjectSupport.ClassPathIconResource("inherited icon");
    }

    @ObjectSupport public String cssClass(){
        return "another-class";
    }

    @ObjectSupport public String layout(){
        return "layout";
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

    //FIXME[CAUSEWAY-2774] method overloading is not allowed - MM validation must fail, but currently does not?
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
