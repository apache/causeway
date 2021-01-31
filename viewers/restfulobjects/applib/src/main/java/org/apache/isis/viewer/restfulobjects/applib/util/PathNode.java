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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

/**
 * @since 1.x {@index}
 */
public class PathNode {

    private static final Pattern NODE = Pattern.compile("^([^\\[]*)(\\[(.+)\\])?$");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern LIST_CRITERIA_SYNTAX = Pattern.compile("^([^=]+)=(.+)$");

    public static final PathNode NULL = new PathNode("", Collections.<String, String> emptyMap());

    public static List<String> split(String path) {
        List<String> parts = _Lists.newArrayList();
        String curr = null;

        final List<String> chunks = _Strings.splitThenStream(path, ".")
                .collect(Collectors.toList());

        for (String part : chunks) {
            if(curr != null) {
                if(part.contains("]")) {
                    curr = curr + "." + part;
                    parts.add(curr);
                    curr = null;
                } else {
                    curr = curr + "." + part;
                }
                continue;
            }
            if(!part.contains("[")) {
                parts.add(part);
                continue;
            }
            if(part.contains("]")) {
                parts.add(part);
            } else {
                curr = part;
            }
        }
        return parts;
    }

    public static PathNode parse(final String path) {
        final Matcher nodeMatcher = NODE.matcher(path);
        if (!nodeMatcher.matches()) {
            return null;
        }
        final int groupCount = nodeMatcher.groupCount();
        if (groupCount < 1) {
            return null;
        }
        final String key = nodeMatcher.group(1);
        final Map<String, String> criteria = _Maps.newHashMap();
        final String criteriaStr = nodeMatcher.group(3);
        if (criteriaStr != null) {

            _Strings.splitThenStream(criteriaStr, WHITESPACE)
            .forEach(criterium->{
                final Matcher keyValueMatcher = LIST_CRITERIA_SYNTAX.matcher(criterium);
                if (keyValueMatcher.matches()) {
                    criteria.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
                } else {
                    // take content as a map criteria
                    criteria.put(criterium, null);
                }
            });

        }

        return new PathNode(key, criteria);
    }

    private final String key;
    private final Map<String, String> criteria;

    private PathNode(final String key, final Map<String, String> criteria) {
        this.key = key;
        this.criteria = Collections.unmodifiableMap(criteria);
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getCriteria() {
        return criteria;
    }

    public boolean hasCriteria() {
        return !getCriteria().isEmpty();
    }

    public boolean matches(final JsonRepresentation repr) {
        if (!repr.isMap()) {
            return false;
        }
        for (final Map.Entry<String, String> criterium : getCriteria().entrySet()) {
            String requiredValue = criterium.getValue();
            if(requiredValue != null) {
                // list syntax
                String actualValue = repr.getString(criterium.getKey());
                if(actualValue == null) {
                    return false;
                }

                // determine if fuzzy match (ie without additional parameters)
                // eg [rel=urn:org.restfulobjects:rel/details;action="list"] matches [rel=urn:org.restfulobjects:rel/details]
                final int actualValueSemiIndex = actualValue.indexOf(";");
                final int requiredValueSemiIndex = requiredValue.indexOf(";");
                if(actualValueSemiIndex != -1 && requiredValueSemiIndex == -1 ) {
                    actualValue = actualValue.substring(0, actualValueSemiIndex);
                }
                if(actualValueSemiIndex == -1 && requiredValueSemiIndex != -1) {
                    requiredValue = requiredValue.substring(0, requiredValueSemiIndex);
                }
                if (!Objects.equals(requiredValue, actualValue)) {
                    return false;
                }
            } else {
                // map syntax
                return repr.getRepresentation(criterium.getKey()) != null;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathNode other = (PathNode) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return key + (criteria.isEmpty() ? "" : criteria);
    }

}
