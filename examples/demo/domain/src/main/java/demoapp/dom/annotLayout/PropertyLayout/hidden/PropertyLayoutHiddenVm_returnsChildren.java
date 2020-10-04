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
package demoapp.dom.annotLayout.PropertyLayout.hidden;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotLayout.PropertyLayout.hidden.child.PropertyLayoutHiddenChildVm;

@Action(
    semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class PropertyLayoutHiddenVm_returnsChildren {

    private final PropertyLayoutHiddenVm propertyLayoutHiddenVm;

//tag::meta-annotation[]
    public List<PropertyLayoutHiddenChildVm> act() {
        return propertyLayoutHiddenVm.getChildren();
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutHiddenVm.getPropertyUsingMetaAnnotation();
    }

}
