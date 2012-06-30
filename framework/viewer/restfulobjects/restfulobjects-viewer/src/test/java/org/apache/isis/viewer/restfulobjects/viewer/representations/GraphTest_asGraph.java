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
package org.apache.isis.viewer.restfulobjects.viewer.representations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.util.Parser;
import org.apache.isis.viewer.restfulobjects.applib.util.PathNode;
import org.apache.isis.viewer.restfulobjects.viewer.representations.GraphUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GraphTest_asGraph {

    @Test
    public void simple() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c,a.b.d,d.b,e,e");
        final Map<PathNode, Map> root = GraphUtil.asGraph(links);

        assertThat(root.size(), is(3));
        final Map<String, Map> nodeA = root.get(PathNode.parse("a"));
        assertThat(nodeA.size(), is(1));
        final Map<String, Map> nodeAB = nodeA.get(PathNode.parse("b"));
        assertThat(nodeAB.size(), is(2));
        final Map<String, Map> nodeABC = nodeAB.get(PathNode.parse("c"));
        assertThat(nodeABC.size(), is(0));
        final Map<String, Map> nodeABD = nodeAB.get(PathNode.parse("d"));
        assertThat(nodeABD.size(), is(0));

        final Map<String, Map> nodeD = root.get(PathNode.parse("d"));
        assertThat(nodeD.size(), is(1));
        final Map<String, Map> nodeDB = nodeD.get(PathNode.parse("b"));
        assertThat(nodeDB.size(), is(0));

        final Map<String, Map> nodeE = root.get(PathNode.parse("e"));
        assertThat(nodeE.size(), is(0));
    }

    @Test
    public void empty() throws Exception {
        final List<List<String>> links = asListOfLists("");
        final Map<PathNode, Map> root = GraphUtil.asGraph(links);

        assertThat(root.size(), is(0));
    }

    @Test
    public void whenNull() throws Exception {
        final Map<PathNode, Map> root = GraphUtil.asGraph(null);

        assertThat(root.size(), is(0));
    }

    private List<List<String>> asListOfLists(final String string) {
        return Parser.forListOfListOfStrings().valueOf(string);
    }
}
