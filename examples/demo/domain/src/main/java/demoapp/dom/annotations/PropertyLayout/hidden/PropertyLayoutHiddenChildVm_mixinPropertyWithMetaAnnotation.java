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
package demoapp.dom.annotations.PropertyLayout.hidden;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Property()
@HiddenEverywhereMetaAnnotation             // <.>
@PropertyLayout(
    describedAs =
        "@HiddenEverywhereMetaAnnotation"
)
@RequiredArgsConstructor
public class PropertyLayoutHiddenChildVm_mixinPropertyWithMetaAnnotation {
    // ...
//end::class[]

    private final PropertyLayoutHiddenChildVm propertyLayoutHiddenChildVm;

    @MemberOrder(name = "meta-annotated", sequence = "2")
    public String prop() {
        return propertyLayoutHiddenChildVm.getPropertyHiddenNowhere();
    }

//tag::class[]
}
//end::class[]
