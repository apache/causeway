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
package org.apache.isis.viewer.restfulobjects.rendering;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;

public class LinkFollowSpecsTest_follow {

    @Test
    public void simple() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.follow("a").isFollowing(), is(true));
        assertThat(linkFollower.follow("a").isTerminated(), is(false));
    }

    @Test
    public void notMatching() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));
        assertThat(linkFollower.follow("x").isTerminated(), is(true));
    }

    @Test
    public void create_noCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.matches(JsonRepresentation.newMap()), is(true));
    }

    @Test
    public void follow_noCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.matches(JsonRepresentation.newMap()), is(true));
    }

    @Test
    public void follow_withSingleListCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a[x=y].b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));

        final LinkFollowSpecs followA = linkFollower.follow("a");

        assertThat(followA.isFollowing(), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap()), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "z")), is(false));
    }

    @Test
    public void follow_withSingleMapCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a[x].b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));

        final LinkFollowSpecs followA = linkFollower.follow("a");

        assertThat(followA.isFollowing(), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "z")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap()), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("p", "z")), is(false));
    }

    @Test
    public void follow_withMultipleCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("a[x=y z=w].b.c");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        assertThat(linkFollower.follow("x").isFollowing(), is(false));

        final LinkFollowSpecs followA = linkFollower.follow("a");

        assertThat(followA.isFollowing(), is(true));

        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "w")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "w", "foo", "bar")), is(true));
        assertThat(followA.matches(JsonRepresentation.newMap()), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "foo", "bar")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "bad")), is(false));
        assertThat(followA.matches(JsonRepresentation.newMap("x", "y", "z", "bad")), is(false));
    }


    @Test
    public void simple_multiplePaths() throws Exception {
        final List<List<String>> links = asListOfLists("a.b.c,x.y.z");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        LinkFollowSpecs followA = linkFollower.follow("a");
        assertThat(followA.isFollowing(), is(true));
        assertThat(followA.isTerminated(), is(false));

        LinkFollowSpecs followX = linkFollower.follow("x");
        assertThat(followX.isFollowing(), is(true));
        assertThat(followX.isTerminated(), is(false));

        LinkFollowSpecs followXY = followX.follow("y");
        assertThat(followXY.isFollowing(), is(true));
        assertThat(followXY.isTerminated(), is(false));

        LinkFollowSpecs followXYZ = followXY.follow("z");
        assertThat(followXYZ.isFollowing(), is(true));
        assertThat(followXYZ.isTerminated(), is(false));

        LinkFollowSpecs followXYZQ = followXY.follow("q");
        assertThat(followXYZQ.isFollowing(), is(false));
        assertThat(followXYZQ.isTerminated(), is(true));
    }

    @Test
    public void multiplePaths_withCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("links[rel=urn:org.restfulobjects:rels/version].x,links[rel=urn:org.restfulobjects:rels/user].y");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        LinkFollowSpecs followRelVersion = linkFollower.follow("links[rel=urn:org.restfulobjects:rels/version]");
        assertThat(followRelVersion.isFollowing(), is(true));
        assertThat(followRelVersion.isTerminated(), is(false));

        assertThat(followRelVersion.follow("x").isFollowing(), is(true));

        LinkFollowSpecs followRelUser = linkFollower.follow("links[rel=urn:org.restfulobjects:rels/user]");
        assertThat(followRelUser.isFollowing(), is(true));
        assertThat(followRelUser.isTerminated(), is(false));
        assertThat(followRelUser.follow("y").isFollowing(), is(true));
    }

    @Test
    public void multiplePaths_withRelFullCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("links[rel=urn:org.restfulobjects:rels/details;action=foo].x,links[rel=urn:org.restfulobjects:rels/details;action=bar].y");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        LinkFollowSpecs followRelVersion = linkFollower.follow("links[rel=urn:org.restfulobjects:rels/details;action=foo]");
        assertThat(followRelVersion.isFollowing(), is(true));
        assertThat(followRelVersion.isTerminated(), is(false));

        assertThat(followRelVersion.follow("x").isFollowing(), is(true));

        LinkFollowSpecs followRelUser = linkFollower.follow("links[rel=urn:org.restfulobjects:rels/details;action=bar]");
        assertThat(followRelUser.isFollowing(), is(true));
        assertThat(followRelUser.isTerminated(), is(false));
        assertThat(followRelUser.follow("y").isFollowing(), is(true));
    }

    @Test
    public void multiplePaths_withRelSimplifiedCriteria() throws Exception {
        final List<List<String>> links = asListOfLists("links[rel=urn:org.restfulobjects:rels/details;action=foo].x,links[rel=urn:org.restfulobjects:rels/details;action=bar].y");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        LinkFollowSpecs followRelVersion = linkFollower.follow("links[rel=urn:org.restfulobjects:rels/details]");
        assertThat(followRelVersion.isFollowing(), is(true));
        assertThat(followRelVersion.isTerminated(), is(false));

        assertThat(followRelVersion.follow("x").isFollowing(), is(true));
    }

    @Test
    public void example_of_eager_loading_of_collection() throws Exception {
        final List<List<String>> links = asListOfLists("members[children].value");

        final LinkFollowSpecs linkFollower = LinkFollowSpecs.create(links);

        LinkFollowSpecs followMembers = linkFollower.follow("members[children]");
        assertThat(followMembers.isFollowing(), is(true));
        assertThat(followMembers.isTerminated(), is(false));

        assertThat(followMembers.follow("value").isFollowing(), is(true));
        assertThat(followMembers.follow("value").isTerminated(), is(false));
    }


    private List<List<String>> asListOfLists(final String string) {
        return Parser.forListOfListOfStrings().valueOf(string);
    }
}
