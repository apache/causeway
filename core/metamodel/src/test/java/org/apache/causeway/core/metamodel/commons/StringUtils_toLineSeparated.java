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
package org.apache.causeway.core.metamodel.commons;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StringUtils_toLineSeparated {

    @Test
    public void convertsCarriageReturnToLineSeparator() throws Exception {
        assertThat(StringExtensions.lineSeparated("ok\n"), is("ok" + System.getProperty("line.separator")));
    }

    @Test
    public void windowsStyleCarriageReturnLeftUnchanged() throws Exception {
        if(!isRunningOnWindows()) return;
        assertThat(StringExtensions.lineSeparated("ok\r\n"), is("ok" + System.getProperty("line.separator")));
    }

    @Test
    public void macStyleCarriageReturnLeftUnchanged() throws Exception {
        if(!isRunningOnWindows()) return;
        assertThat(StringExtensions.lineSeparated("ok\r"), is("ok\r"));
    }

    private static boolean isRunningOnWindows() {
        return Objects.equals(System.getProperty("file.separator"), "\\");
    }

}
