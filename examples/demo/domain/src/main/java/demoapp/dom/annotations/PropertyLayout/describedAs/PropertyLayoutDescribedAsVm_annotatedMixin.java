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
package demoapp.dom.annotations.PropertyLayout.describedAs;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

//tag::class[]
//@Property() // TODO: ISIS-2405: this fails as attempts to invoke as an action, and heuristics replace null with the view model object.
@PropertyLayout(
    describedAs =
            "Mixin described using @PropertyLayout(describedAs = \"...\")"
    , hidden = Where.ALL_TABLES
)
@RequiredArgsConstructor
public class PropertyLayoutDescribedAsVm_annotatedMixin {

    private final PropertyLayoutDescribedAsVm propertyLayoutDescribedAsVm;

    @MemberOrder(name = "contributed", sequence = "1")
    public String prop() {
        return propertyLayoutDescribedAsVm.getPropertyUsingAnnotation();
    }

}
//end::class[]
