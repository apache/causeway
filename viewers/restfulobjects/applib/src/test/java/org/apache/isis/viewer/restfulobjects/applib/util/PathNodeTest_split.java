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
package org.apache.isis.viewer.restfulobjects.applib.util;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathNodeTest_split {

    @Test
    public void simple() throws Exception {
        List<String> parts = PathNode.split("a.b.c");
        assertThat(parts.size(), is(3));
    }

    @Test
    public void withBrackets() throws Exception {
        List<String> parts = PathNode.split("a[x=y].b[p=q].c");
        assertThat(parts.size(), is(3));
    }

    @Test
    public void withBracketsAndContainedDots() throws Exception {
        List<String> parts = PathNode.split("a[m.n=s.t].b[p=q].c");
        assertThat(parts.size(), is(3));
    }

    @Test
    public void realistic() throws Exception {
        List<String> parts = PathNode.split("links[rel=urn.restfulobjects:rels/user]");
        assertThat(parts.size(), is(1));
        assertThat(parts.get(0), is("links[rel=urn.restfulobjects:rels/user]"));
    }


}
