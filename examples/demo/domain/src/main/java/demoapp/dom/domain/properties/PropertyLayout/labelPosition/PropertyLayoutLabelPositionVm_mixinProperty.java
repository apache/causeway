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
package demoapp.dom.domain.properties.PropertyLayout.labelPosition;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Property()
@PropertyLayout(
    labelPosition = LabelPosition.TOP           // <.>
    , describedAs =
        "@PropertyLayout(labelPosition = TOP)"
    , hidden = Where.ALL_TABLES,
    fieldSetId = "contributed", sequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutLabelPositionVm_mixinProperty {
    // ...
//end::class[]

    private final PropertyLayoutLabelPositionVm propertyLayoutLabelPositionVm;

    public String prop() {
        return propertyLayoutLabelPositionVm.getPropertyUsingAnnotation();
    }

//tag::class[]
}
//end::class[]
