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
package org.apache.causeway.core.config.viewer.wicket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.causeway.core.config.viewer.web.WebAppContextPath;

class WebAppContextPath_setContextPath_Test {

    private WebAppContextPath webAppContextPath;

    @BeforeEach
    void setup() {
        webAppContextPath = new WebAppContextPath();
    }

    @Test
    void when_null() {
        webAppContextPath.setContextPath(null);

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("");
    }

    @Test
    void when_empty() {
        webAppContextPath.setContextPath("");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("");
    }
    
    @Test
    void when_slash() {
        webAppContextPath.setContextPath("/");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("");
    }

    @Test
    void when_no_leading_slash() {
        webAppContextPath.setContextPath("abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_leading_slash() {
        webAppContextPath.setContextPath("/abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_multiple_leading_slashes() {
        webAppContextPath.setContextPath("//abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_no_trailing_slash() {
        webAppContextPath.setContextPath("/abc");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_trailing_slash() {
        webAppContextPath.setContextPath("/abc/");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_multiple_trailing_slashes() {
        webAppContextPath.setContextPath("/abc//");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc");
    }

    @Test
    void when_multiple_contains_slashes() {
        webAppContextPath.setContextPath("/abc/def/");

        Assertions.assertThat(webAppContextPath.getContextPath()).isEqualTo("/abc/def");
    }

}