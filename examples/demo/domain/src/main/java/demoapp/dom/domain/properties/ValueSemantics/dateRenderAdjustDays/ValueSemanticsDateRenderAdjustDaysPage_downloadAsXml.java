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
package demoapp.dom.domain.properties.ValueSemantics.dateRenderAdjustDays;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@RequiredArgsConstructor
public class ValueSemanticsDateRenderAdjustDaysPage_downloadAsXml {
    // ...
//end::class[]

    private final ValueSemanticsDateRenderAdjustDaysPage page;

//tag::class[]
    @MemberSupport public Clob act(final String fileName) {
        val xml = jaxbService.toXml(page);
        return Clob.of(fileName, NamedWithMimeType.CommonMimeType.XML, xml);
    }
    // ...
//end::class[]

    @MemberSupport public String default0Act() {
        return "ValueSemantics.dateRenderAdjustDaysPage.xml";
    }

//tag::class[]
    @Inject JaxbService jaxbService;
}
//end::class[]
