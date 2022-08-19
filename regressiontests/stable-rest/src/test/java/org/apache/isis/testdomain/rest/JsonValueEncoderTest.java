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
package org.apache.isis.testdomain.rest;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;

import lombok.val;

@SpringBootTest(classes={
        Configuration_headless.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class
})
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true") //TODO WIP
@TestPropertySource(IsisPresets.UseLog4j2Test)
class JsonValueEncoderTest {

    @Inject MetaModelContext mmc;

    @Test
    void whenBlob() {
        val value = Blob.of("a Blob", CommonMimeType.BIN, new byte[] {1, 2, 3});
        val valueAdapter = mmc.getObjectManager().adapt(value);

        val jsonValueEncoder = JsonValueEncoder.forTesting(mmc.getSpecificationLoader());
        val representation = JsonRepresentation.newMap();
        jsonValueEncoder.appendValueAndFormat(valueAdapter, representation, null, false);

        System.err.printf("representation %s%n", representation);

        //representation.getString("value");

        //representation.isMap("value");

        //assertTrue(representation.isString("value"));

        //assertEquals(new BigDecimal("12345678901234567890.1234"), representation.getBigDecimal("value"));
        //assertEquals("big-decimal(27,4)", representation.getString("format"));
        //assertEquals("javamathbigdecimal", representation.getString("extensions.x-isis-format"));
    }

}
