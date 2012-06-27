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
package org.apache.isis.viewer.json.viewer.representations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.util.Parser;

public class LinkFollowerTest_follow {

    @Test
    public void simple() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        assertThat(linkFollower.follow("a").isFollowing(), is(true));
        assertThat(linkFollower.follow("a").isTerminated(), is(false));
    }

    @Test
    public void notMatching() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));
        assertThat(linkFollower.follow("x").isTerminated(), is(true));
    }

    @Test
    public void create_noCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        assertThat(linkFollower.criteria().size(), is(0));
        assertThat(linkFollower.matches(JsonRepresentation.newMap()), is(true));
    }

    @Test
    public void follow_noCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        final LinkFollower followA = linkFollower.follow("a");

        assertThat(followA.criteria().size(), is(0));
        assertThat(linkFollower.matches(JsonRepresentation.newMap()), is(true));
    }

    @Test
    public void follow_withSingleCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a[x=y].b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));

        final LinkFollower followA = linkFollower.follow("a");

        assertThat(followA.isFollowing(), is(true));
        final Map<String, String> criteria = followA.criteria();
        assertThat(criteria.size(), is(1));
        assertThat(criteria.get("x"), is("y"));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap()), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "z")), is(false));
    }

    @Test
    public void follow_withMultipleCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a[x=y z=w].b.c");

        final LinkFollower linkFollower = LinkFollower.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));

        final LinkFollower followA = linkFollower.follow("a");

        assertThat(followA.isFollowing(), is(true));
        final Map<String, String> criteria = followA.criteria();
        assertThat(criteria.size(), is(2));

        assertThat(criteria.get("x"), is("y"));
        assertThat(criteria.get("z"), is("w"));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "w")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "w", "foo", "bar")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap()), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "foo", "bar")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "bad")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "bad")), is(false));
    }

    private List<List<String>> asListOfLists(final String string) {
        return Parser.forListOfListOfStrings().valueOf(string);
    }
}
