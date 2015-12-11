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
package org.apache.isis.core.metamodel.services.swagger.internal;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SwaggerSpecGeneratorTest {

    public static class TagFor extends SwaggerSpecGeneratorTest {

        @Test
        public void fullyQualifiedClass() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("foo.bar.Abc");
            assertThat(tag, is(equalTo("bar")));
        }

        @Test
        public void jaxb() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("todoapp.app.viewmodels.todoitem.v1_0.ToDoItemDto");
            assertThat(tag, is(equalTo("todoitem")));
        }

        @Test
        public void schemaClass() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("bar.Abc");
            assertThat(tag, is(equalTo("bar")));
        }

        @Test
        public void noPackage() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("Abc");
            assertThat(tag, is(equalTo("Abc")));
        }

        @Test
        public void isisAddons() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("org.isisaddons.module.security.app.feature.ApplicationClass");
            assertThat(tag, is(equalTo("isisaddons security")));
        }

        @Test
        public void applib() throws Exception {
            String tag = SwaggerSpecGenerator.tagFor("org.apache.isis.applib.fixturescripts.FixtureResult");
            assertThat(tag, is(equalTo("> apache isis internals")));
        }

    }

}