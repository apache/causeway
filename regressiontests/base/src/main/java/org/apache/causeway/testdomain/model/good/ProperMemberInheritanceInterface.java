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

public interface ProperMemberInheritanceInterface {

    @ObjectSupport
    default String title() {
        return "inherited title";
    }

    @ObjectSupport default ObjectSupport.IconResource icon(final ObjectSupport.IconWhere iconWhere) {
        return new ObjectSupport.ClassPathIconResource("inherited icon");
    }

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    default void sampleAction() {
    }

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    default String getSampleProperty() {
        return null;
    }

    @Collection
    @CollectionLayout(named = "foo", describedAs = "bar")
    default List<String> getSampleCollection() {
        return null;
    }

    // -- OVERRIDING TESTS

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    default void sampleActionOverride() {
    }

    @Action
    @ActionLayout(named = "foo", describedAs = "bar")
    default void sampleActionOverrideWithParam(final String x) {
    }

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    default String getSamplePropertyOverride() {
        return null;
    }

}
