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
package demoapp.dom.domain.properties.PropertyLayout.repainting;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Repainting;
import org.apache.isis.applib.value.Blob;

import lombok.RequiredArgsConstructor;

//tag::meta-annotation-overridden[]
@RepaintingRepaintMetaAnnotation                                    // <.>
@Property()
@PropertyLayout(
    repainting = Repainting.NO_REPAINT                              // <.>
    , describedAs =
        "@RepaintingRepaintMetaAnnotation " +
        "@PropertyLayout(repainting = NO_REPAINT)",
    group = "meta-annotated-overridden", sequence = "2"
)
@RequiredArgsConstructor
public class PropertyLayoutRepaintingVm_mixinPropertyWithMetaAnnotationOverridden {
    // ...
//end::meta-annotation-overridden[]

    private final PropertyLayoutRepaintingVm propertyLayoutRepaintingVm;

//    @PdfJsViewer
    public Blob prop() {
        return propertyLayoutRepaintingVm.getPropertyUsingAnnotation();
    }

//tag::meta-annotation-overridden[]
}
//end::meta-annotation-overridden[]
