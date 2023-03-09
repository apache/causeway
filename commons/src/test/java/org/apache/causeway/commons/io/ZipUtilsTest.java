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
package org.apache.causeway.commons.io;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import lombok.SneakyThrows;
import lombok.val;

class ZipUtilsTest {

    private byte[] bytes;

    @BeforeEach
    void setup() {
        val rd = new Random(9999); // fixed seed - reproducible tests
        this.bytes = new byte[16*1024];
        rd.nextBytes(bytes);
    }

    @Test
    void zipUnzipRountrip() throws Exception {
        assertArrayEquals(bytes, unZip(zip(bytes)));
    }

    // -- HELPER

    private static byte[] zip(final byte[] unzipped) {
        val zipBuilder = ZipUtils.zipEntryBuilder();
        zipBuilder.add("test", unzipped);
        return zipBuilder.toBytes();
    }

    @SneakyThrows
    private static byte[] unZip(final byte[] zipped) {
        return ZipUtils.streamZipEntries(DataSource.ofBytes(zipped))
        .findFirst()
        .map(entry->entry.bytes())
        .orElseGet(()->new byte[0]);
    }

}
