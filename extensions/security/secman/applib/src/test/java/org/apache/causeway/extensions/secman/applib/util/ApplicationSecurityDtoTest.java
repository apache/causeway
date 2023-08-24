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
package org.apache.causeway.extensions.secman.applib.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.TextUtils;

import lombok.val;

class ApplicationSecurityDtoTest {

    /**
     * Read DTO from accompanied YAML file, then re-export and see whether those 2 match.
     */
    @Test
    void roundtripViaYaml() {

        if(this.getClass().getName().contains(".isis.")) return; // disabled for legacy CI build

        val yamlSource = DataSource.ofInputStreamEagerly(
                getClass().getResourceAsStream("secman-permissions.yml"));

        val yamlBeforeRoundtrip = yamlSource.tryReadAsStringUtf8()
                .valueAsNonNullElseFail();

        final ApplicationSecurityDto dto = ApplicationSecurityDto.tryRead(yamlSource)
                .valueAsNonNullElseFail();

        val yamlAfterRoundtrip = dto.toYaml();

        assertEquals(
                TextUtils.readLines(yamlBeforeRoundtrip),
                TextUtils.readLines(yamlAfterRoundtrip));
    }

}
