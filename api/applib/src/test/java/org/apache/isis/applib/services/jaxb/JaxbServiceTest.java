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
package org.apache.isis.applib.services.jaxb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.resources._Xml;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.val;

class JaxbServiceTest {

    private JaxbService.Simple jaxbServiceSimple;
    private ActionInvocationDto sampleDto;
    
    @BeforeEach
    void setUp() throws Exception {
        jaxbServiceSimple = new JaxbService.Simple();
        sampleDto = getSample();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test @Disabled("fails because ActionInvocationDto has no @XmlRootElement annoation")
    void roundtrip() {
        val xml = jaxbServiceSimple.toXml(sampleDto);
        val clone = jaxbServiceSimple.<ActionInvocationDto>fromXml(ActionInvocationDto.class, xml);
        assertEquals(sampleDto, clone);
    }
    
    @Test
    void clone_usingUtility() {
        val dto = getSample();
        assertDtoEquals(dto, _Xml.clone(dto).orElseFail());
    }
    
    // -- HELPER
    
    private ActionInvocationDto getSample() {
        val dto = new ActionInvocationDto();
        dto.setTitle("hello");
        dto.setUser("world");
        return dto;
    }
    
    private void assertDtoEquals(ActionInvocationDto a, ActionInvocationDto b) {
        assertEquals(a.getTitle(), b.getTitle());
        assertEquals(a.getUser(), b.getUser());
    }
    

}
