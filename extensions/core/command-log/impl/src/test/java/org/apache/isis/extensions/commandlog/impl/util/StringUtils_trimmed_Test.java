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
package org.apache.isis.extensions.commandlog.impl.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtils_trimmed_Test {

    @Test
    public void fits() {
        Assertions.assertThat(StringUtils.trimmed("abcde", 5)).isEqualTo("abcde");
    }

    @Test
    public void needs_to_be_trimmed() {
        Assertions.assertThat(StringUtils.trimmed("abcde", 4)).isEqualTo("a...");
    }

    @Test
    public void when_null() {
        Assertions.assertThat(StringUtils.trimmed(null, 4)).isNull();
    }

    @Test
    public void when_empty() {
        Assertions.assertThat(StringUtils.trimmed("", 4)).isEqualTo("");
    }

}