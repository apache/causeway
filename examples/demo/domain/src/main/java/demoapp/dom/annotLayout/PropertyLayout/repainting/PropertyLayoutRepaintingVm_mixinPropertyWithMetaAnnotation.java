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
package demoapp.dom.annotLayout.PropertyLayout.repainting;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.value.Blob;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Property()
@RepaintingNoRepaintMetaAnnotation            // <.>
@PropertyLayout(
    describedAs =
        "@RepaintingNoRepaintMetaAnnotation"
)
@RequiredArgsConstructor
public class PropertyLayoutRepaintingVm_mixinPropertyWithMetaAnnotation {
    // ...
//end::class[]

    private final PropertyLayoutRepaintingVm propertyLayoutRepaintingVm;

//    @PdfJsViewer
    @MemberOrder(name = "meta-annotated", sequence = "2")
    public Blob prop() {
        return propertyLayoutRepaintingVm.getPropertyUsingAnnotation();
    }

//tag::class[]
}
//end::class[]
