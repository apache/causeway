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
package demoapp.dom.domain.properties.ValueSemantics;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.properties.ValueSemantics.dateRenderAdjustDays.ValueSemanticsDateRenderAdjustDaysPage;
import demoapp.dom.domain.properties.ValueSemantics.percentage.ValueSemanticsProviderPercentagePage;

@Named("demo.ValueSemanticsMenu")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
//@Log4j2
public class ValueSemanticsMenu {

//tag::create-page[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-step-forward", describedAs = "Inclusive and exclusive date ranges")
    public ValueSemanticsDateRenderAdjustDaysPage dateRenderAdjustDays(){
        var page = new ValueSemanticsDateRenderAdjustDaysPage();
        page.setStartDate(LocalDate.of(2012,1,1));
        page.setEndDate(page.getStartDate().plusDays(7));               // <.>
        return page;
    }
//end::create-page[]

//tag::percentage[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-percent", describedAs = "Use a custom ValueSemanticsProvider with meta-annotation to customise the display")
    public ValueSemanticsProviderPercentagePage percentage(){
        var page = new ValueSemanticsProviderPercentagePage();
        page.setPercentage(new BigDecimal(".95"));
        return page;
    }
//end::percentage[]

}
