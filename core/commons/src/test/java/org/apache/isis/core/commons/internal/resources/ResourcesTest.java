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
package org.apache.isis.core.commons.internal.resources;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void localRelativeResourcePathDetection() {

        assertTrue(_Resources.isLocalResource("/hello"));
        assertTrue(_Resources.isLocalResource("/hello/world"));
        assertTrue(_Resources.isLocalResource("/hello//world"));

        assertTrue(_Resources.isLocalResource("hello"));
        assertTrue(_Resources.isLocalResource("hello/world"));
        assertTrue(_Resources.isLocalResource("hello//world"));

    }

    @Test
    void externalResourcePathDetection() {

        assertFalse(_Resources.isLocalResource("http://hello.world"));
        assertFalse(_Resources.isLocalResource("http://localhost:8080/hello"));
        assertFalse(_Resources.isLocalResource("http://localhost:8080/hello"));
        assertFalse(_Resources.isLocalResource("http://127.0.0.1:8080/hello"));

        assertFalse(_Resources.isLocalResource("https://hello.world"));
        assertFalse(_Resources.isLocalResource("https://localhost:8080/hello"));
        assertFalse(_Resources.isLocalResource("https://localhost:8080/hello"));
        assertFalse(_Resources.isLocalResource("https://127.0.0.1:8080/hello"));

    }

}
