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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class OpenApiModelFactoryTest {

    @Test
    public void testAddReference() throws Exception {

        _OpenApiModelFactory context =
                new _OpenApiModelFactory(null, null, null, new TaggerDefault(),
                        new ClassExcluderDefault(), new ValueSchemaFactoryDefault());

        context.addSwaggerReference("foo");
        context.addSwaggerReference("bar");
        context.addSwaggerReference("baz");

        context.addSwaggerDefinition("foo");
        context.addSwaggerDefinition("box");

        Set<String> referencesWithoutDefinition = context.getReferencesWithoutDefinition();
        assertThat(referencesWithoutDefinition.size(), is(2));
        assertThat(referencesWithoutDefinition, contains("bar", "baz"));

    }
}