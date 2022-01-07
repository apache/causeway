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
package demoapp.dom.domain.properties.PropertyLayout.renderDay;

import java.time.LocalDate;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.RenderDay;
import org.apache.isis.applib.annotations.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
    associateWith = "endDateUsingMetaAnnotationButOverridden"
    , sequence = "1")
@RequiredArgsConstructor
public class PropertyLayoutRenderDayVm_updateEndDateWithMetaAnnotationOverridden {

    private final PropertyLayoutRenderDayVm propertyLayoutRenderDayVm;

//tag::meta-annotation-overridden[]
    @MemberSupport public PropertyLayoutRenderDayVm act(
            @RenderDayMetaAnnotationStartDateInclusive          // <.>
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                renderDay = RenderDay.AS_DAY_BEFORE             // <.>
                , describedAs =
                    "@RenderDayMetaAnnotationStartDateInclusive " +
                    "@ParameterLayout(renderDay = AS_DAY_BEFORE)"
            )
            final LocalDate endDate) {
        propertyLayoutRenderDayVm.setEndDateUsingMetaAnnotationButOverridden(endDate);
        return propertyLayoutRenderDayVm;
    }
//end::meta-annotation-overridden[]
    @MemberSupport public LocalDate default0Act() {
        return propertyLayoutRenderDayVm.getEndDateUsingMetaAnnotationButOverridden();
    }

}
