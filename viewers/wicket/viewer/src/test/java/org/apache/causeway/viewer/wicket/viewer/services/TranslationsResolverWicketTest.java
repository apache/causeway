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
package org.apache.causeway.viewer.wicket.viewer.services;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.commons.internal.base._Files;

import lombok.val;

class TranslationsResolverWicketTest {

    private File tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        tempDir = _Files.tempDir("foo");
    }

    @Test
    public void simple() throws Exception {
        final File file = TranslationsResolverWicket.newFile(tempDir.getAbsolutePath(), "bar").toFile();
        val expected = new File(tempDir, "bar");
        assertThat(file.getAbsolutePath(), is(expected.getAbsolutePath()));
    }

    @Test
    public void nestedChild() throws Exception {
        final File file = TranslationsResolverWicket.newFile(tempDir.getAbsolutePath(), "bar/baz").toFile();
        val expected = new File(tempDir, "bar/baz");
        assertThat(file.getAbsolutePath(), is(expected.getAbsolutePath()));
    }

}