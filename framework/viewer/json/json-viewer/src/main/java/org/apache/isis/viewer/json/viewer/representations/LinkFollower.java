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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.util.PathNode;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class LinkFollower {

    public final static LinkFollower create(final List<List<String>> links) {
        final Map<PathNode, Map> graph = GraphUtil.asGraph(links);
        return new LinkFollower(graph, Mode.FOLLOWING, PathNode.NULL);
    }

    private enum Mode {
        FOLLOWING, TERMINATED;
    }

    private final Map<PathNode, Map> graph;
    private final Mode mode;
    private final PathNode root;

    private LinkFollower(final Map<PathNode, Map> graph, final Mode mode, final PathNode root) {
        this.graph = graph;
        this.mode = mode;
        this.root = root;
    }

    /**
     * A little algebra...
     */
    public LinkFollower follow(final String pathTemplate, final Object... args) {
        final String path = String.format(pathTemplate, args);
        if (path == null) {
            return terminated(PathNode.NULL);
        }
        if (mode == Mode.TERMINATED) {
            return terminated(this.root);
        }
        final PathNode node = PathNode.parse(path);
        if (mode == Mode.FOLLOWING) {
            final Map<PathNode, Map> remaining = graph.get(node);
            if (remaining != null) {
                final PathNode key = findKey(node);
                return new LinkFollower(remaining, Mode.FOLLOWING, key);
            } else {
                return terminated(node);
            }
        }
        return terminated(node);
    }

    /**
     * somewhat bizarre, but we have to find the actual node that is in the
     * graph; the one we matching on doesn't match on the
     * {@link PathNode#getCriteria()} map.
     */
    private PathNode findKey(final PathNode node) {
        final Set<PathNode> keySet = graph.keySet();
        for (final PathNode key : keySet) {
            if (key.equals(node)) {
                return key;
            }
        }
        // shouldn't happen
        return node;
    }

    private static LinkFollower terminated(final PathNode node) {
        return new LinkFollower(null, Mode.TERMINATED, node);
    }

    /**
     * Not public API; use {@link #matches(JsonRepresentation)}.
     */
    boolean isFollowing() {
        return mode == Mode.FOLLOWING;
    }

    public boolean isTerminated() {
        return mode == Mode.TERMINATED;
    }

    public Map<String, String> criteria() {
        return Collections.unmodifiableMap(root.getCriteria());
    }

    /**
     * Ensure that every key present in the provided map matches the criterium.
     * 
     * <p>
     * Any keys in the criterium that are not present in the map will be
     * ignored.
     */
    public boolean matches(final JsonRepresentation link) {
        if (!isFollowing()) {
            return false;
        }
        return root == null || root.matches(link);
    }

}
