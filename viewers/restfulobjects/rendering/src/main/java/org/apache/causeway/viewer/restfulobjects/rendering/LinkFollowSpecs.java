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
package org.apache.causeway.viewer.restfulobjects.rendering;

import java.util.Collections;
import java.util.List;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.util.PathNode;
import org.apache.causeway.viewer.restfulobjects.rendering.util.FollowSpecUtil;

public final class LinkFollowSpecs {

    public static final LinkFollowSpecs create(final List<List<String>> links) {
        final List<List<PathNode>> specs = FollowSpecUtil.asFollowSpecs(links);
        return new LinkFollowSpecs(specs, Mode.FOLLOWING, null);
    }

    private enum Mode {
        FOLLOWING, TERMINATED;
    }

    private final List<List<PathNode>> pathSpecs;
    private final Mode mode;
    // don't care about the key, just the criteria
    private final List<PathNode> criteriaSpecs;

    private LinkFollowSpecs(final List<List<PathNode>> pathSpecs, final Mode mode, final List<PathNode> criteriaSpecs) {
        this.pathSpecs = pathSpecs;
        this.mode = mode;
        this.criteriaSpecs = criteriaSpecs;
    }

    /**
     * A little algebra...
     */
    public LinkFollowSpecs follow(final String pathTemplate, final Object... args) {
        final String path = String.format(pathTemplate, args);
        if (path == null) {
            return terminated();
        }
        if (mode == Mode.TERMINATED) {
            return terminated();
        }
        final PathNode candidate = PathNode.parse(path);
        if (mode == Mode.FOLLOWING) {
            List<List<PathNode>> remainingPathSpecs = _Lists.newArrayList();
            List<PathNode> firstSpecs = _Lists.newArrayList();
            for(List<PathNode> spec: pathSpecs) {
                if(spec.isEmpty()) {
                    continue;
                }
                PathNode first = spec.get(0);
                if(candidate.equals(first)) {
                    List<PathNode> remaining = spec.subList(1, spec.size());
                    firstSpecs.add(first);
                    remainingPathSpecs.add(remaining);
                }
            }
            if(!remainingPathSpecs.isEmpty()) {
                return new LinkFollowSpecs(remainingPathSpecs, Mode.FOLLOWING, firstSpecs);
            }
            return terminated();
        }
        return terminated();
    }

    private static LinkFollowSpecs terminated() {
        return new LinkFollowSpecs(Collections.<List<PathNode>>emptyList(), Mode.TERMINATED, Collections.<PathNode>emptyList());
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

    /**
     * Ensure that every key present in the provided map matches the criterium.
     *
     * <p>
     * Any keys in the criterium are ignored (these were matched on during the
     * {@link #follow(String, Object...)} call).
     */
    public boolean matches(final JsonRepresentation jsonRepr) {
        if (!isFollowing()) {
            return false;
        }
        if(criteriaSpecs == null) {
            return true;
        }
        for (PathNode criteriaSpec : criteriaSpecs) {
            if(criteriaSpec.matches(jsonRepr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return mode + " : " + criteriaSpecs + " : " + pathSpecs;
    }

}
