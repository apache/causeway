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
package org.apache.causeway.viewer.restfulobjects.applib.util;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PathNodeTest_parse {

    @Test
    public void simple() throws Exception {
        final PathNode node = PathNode.parse("foo");
        assertThat(node.key(), is("foo"));
        assertThat(node.criteria().isEmpty(), is(true));
    }

    @Test
    public void oneCriterium() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz]");
        assertThat(node.key(), is("foo"));
        final Map<String, String> criteria = node.criteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(1));
        assertThat(criteria.get("bar"), is("coz"));
    }

    @Test
    public void moreThanOneCriterium() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz dat=ein]");
        assertThat(node.key(), is("foo"));
        final Map<String, String> criteria = node.criteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(2));
        assertThat(criteria.get("bar"), is("coz"));
        assertThat(criteria.get("dat"), is("ein"));
    }

    @Test
    public void whiteSpace() throws Exception {
        final PathNode node = PathNode.parse("foo[bar=coz\tdat=ein]");
        assertThat(node.key(), is("foo"));
        final Map<String, String> criteria = node.criteria();
        assertThat(criteria.isEmpty(), is(false));
        assertThat(criteria.size(), is(2));
        assertThat(criteria.get("bar"), is("coz"));
        assertThat(criteria.get("dat"), is("ein"));
    }

}
