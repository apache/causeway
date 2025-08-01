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
package org.apache.causeway.applib.services.jaxb;

import jakarta.xml.bind.JAXBContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import lombok.SneakyThrows;

class JaxbServiceTest {

    private JaxbService simple;
    private ActionInvocationDto sampleDto;

    @BeforeEach
    void setUp() throws Exception {
        simple = JaxbService.simple();
        sampleDto = getSample();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test @Disabled("fails because ActionInvocationDto has no @XmlRootElement annonation")
    void roundtrip() {
        var xml = simple.toXml(sampleDto);
        var clone = simple.<ActionInvocationDto>fromXml(ActionInvocationDto.class, xml);
        assertEquals(sampleDto, clone);
    }

    @Test @SneakyThrows
    void clone_usingUtility() {
        // test prerequisites
        assertNotNull(JAXBContext.newInstance(ActionInvocationDto.class));

        var dto = getSample();
        assertDtoEquals(dto, JaxbUtils.mapperFor(ActionInvocationDto.class, opts->opts.allowMissingRootElement(true))
                .tryClone(dto)
                .ifFailureFail()
                .getValue().orElseThrow());
    }

    // -- HELPER

    private ActionInvocationDto getSample() {
        var dto = new ActionInvocationDto();
        dto.setLogicalMemberIdentifier("customer.Customer#placeOrder");
        return dto;
    }

    private void assertDtoEquals(final ActionInvocationDto a, final ActionInvocationDto b) {
        assertEquals(a.getLogicalMemberIdentifier(), b.getLogicalMemberIdentifier());
    }

}
