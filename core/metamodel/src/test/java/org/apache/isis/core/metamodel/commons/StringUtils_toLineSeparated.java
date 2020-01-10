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

package org.apache.isis.core.metamodel.commons;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class StringUtils_toLineSeparated {

    @Test
    public void convertsCarriageReturnToLineSeparator() throws Exception {
        assertThat(StringExtensions.lineSeparated("ok\n"), is("ok" + System.getProperty("line.separator")));
    }

    @Test
    public void windowsStyleCarriageReturnLeftUnchanged() throws Exception {
        assumeThatRunningOnWindows(); // ie windows
        assertThat(StringExtensions.lineSeparated("ok\r\n"), is("ok" + System.getProperty("line.separator")));
    }

    @Test
    public void macStyleCarriageReturnLeftUnchanged() throws Exception {
        assumeThatRunningOnWindows(); // ie windows
        assertThat(StringExtensions.lineSeparated("ok\r"), is("ok\r"));
    }

    private static void assumeThatRunningOnWindows() {
        assumeThat(System.getProperty("file.separator"), is(equalTo("\\")));
    }

}
