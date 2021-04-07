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
package demoapp.dom.domain.properties.Property.mustSatisfy;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "customerAgePropertyUsingMetaAnnotationButOverridden"
)
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class PropertyMustSatisfyVm_updateWithMetaAnnotationOverridden {

    private final PropertyMustSatisfyVm propertyMustSatisfyVm;

//tag::meta-annotation-overridden[]
    public PropertyMustSatisfyVm act(
            @MustSatisfyOfWorkingAgeMetaAnnotation                  // <.>
            @Parameter(
                mustSatisfy = OfRetirementAgeSpecification.class    // <.>
            )
            @ParameterLayout(
                describedAs =
                    "@MustSatisfyOfWorkingAgeMetaAnnotation " +
                    "@ParameterLayout(mustSatisfy = OfRetirementAgeSpecification.class)"
            )
            final Integer customerAgeParameterUsingMetaAnnotationButOverridden) {
        propertyMustSatisfyVm.setCustomerAgePropertyUsingMetaAnnotationButOverridden(customerAgeParameterUsingMetaAnnotationButOverridden);
        return propertyMustSatisfyVm;
    }
//end::meta-annotation-overridden[]
    public Integer default0Act() {
        return propertyMustSatisfyVm.getCustomerAgePropertyUsingMetaAnnotationButOverridden();
    }

}
