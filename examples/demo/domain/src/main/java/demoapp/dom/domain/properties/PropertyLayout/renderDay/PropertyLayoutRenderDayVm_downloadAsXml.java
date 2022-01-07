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

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
)
@RequiredArgsConstructor
public class PropertyLayoutRenderDayVm_downloadAsXml {

    private final PropertyLayoutRenderDayVm propertyLayoutRenderDayVm;

    @MemberSupport public Clob act(final String fileName) {
        val xml = jaxbService.toXml(propertyLayoutRenderDayVm);
        return Clob.of(fileName, NamedWithMimeType.CommonMimeType.XML, xml);
    }

    @MemberSupport public String default0Act() {
        return "PropertyLayoutRenderVm.xml";
    }

    @Inject
    JaxbService jaxbService;

}
//end::class[]
