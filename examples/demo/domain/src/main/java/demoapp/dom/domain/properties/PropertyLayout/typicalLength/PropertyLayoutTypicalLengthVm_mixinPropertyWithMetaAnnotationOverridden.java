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
package demoapp.dom.domain.properties.PropertyLayout.typicalLength;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.RequiredArgsConstructor;

//tag::meta-annotation-overridden[]
@TypicalLengthMetaAnnotation                                // <.>
@Property()
@PropertyLayout(
    typicalLength = 3                                       // <.>
    , describedAs =
        "@TypicalLengthMetaAnnotation @PropertyLayout(...)",
    fieldSet = "meta-annotated-overridden", sequence = "2"
)
@RequiredArgsConstructor
public class PropertyLayoutTypicalLengthVm_mixinPropertyWithMetaAnnotationOverridden {
    // ...
//end::meta-annotation-overridden[]

    private final PropertyLayoutTypicalLengthVm propertyLayoutTypicalLengthVm;

    public String prop() {
        return propertyLayoutTypicalLengthVm.getPropertyUsingAnnotation();
    }

//tag::meta-annotation-overridden[]
}
//end::meta-annotation-overridden[]
