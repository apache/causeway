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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TaggerTest {

    TaggerDefault taggerDefault;

    @Before
    public void setUp() throws Exception {
        taggerDefault = new TaggerDefault();
    }

    public static class TagFor extends TaggerTest {

        @Test
        public void fullyQualifiedClass() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("foo.bar.Abc", null);
            assertThat(tag, is(equalTo("bar")));
        }

        @Test
        public void jaxb() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("todoapp.app.viewmodels.todoitem.v1_0.ToDoItemDto", null);
            assertThat(tag, is(equalTo("todoitem")));
        }

        @Test
        public void schemaClass() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("bar.Abc", null);
            assertThat(tag, is(equalTo("bar")));
        }

        @Test
        public void noPackage() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("Abc", null);
            assertThat(tag, is(equalTo("Abc")));
        }

        @Test
        public void internals() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("org.apache.isis.Xxx", null);
            assertThat(tag, is(equalTo(". apache isis internals")));
        }

        @Test
        public void applib() throws Exception {
            String tag = taggerDefault.tagForLogicalTypeName("isis.conf.ConfigurationServiceMenu", null);
            assertThat(tag, is(equalTo(". apache isis conf")));
        }

    }

}
