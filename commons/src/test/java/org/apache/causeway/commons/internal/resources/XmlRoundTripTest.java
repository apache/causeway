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
package org.apache.causeway.commons.internal.resources;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.val;

class XmlRoundTripTest {

    @Test @SneakyThrows
    void test() {
        // test prerequisites
        assertNotNull(JAXBContext.newInstance(SampleDto.class));

        val dto = getSample();
        assertEquals(dto, _Xml.clone(dto).getValue().orElseThrow());
    }

    // -- HELPER

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "sampleDto")
    @Data @EqualsAndHashCode
    public static class SampleDto {
        @XmlElement(required = true)
        protected String content;
    }

    private SampleDto getSample() {
        val dto = new SampleDto();
        dto.setContent("Hallo World!");
        return dto;
    }

}
