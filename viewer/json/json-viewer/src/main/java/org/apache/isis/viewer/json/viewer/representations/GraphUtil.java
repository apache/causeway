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

import com.google.common.collect.Maps;

import org.apache.isis.viewer.json.applib.util.PathNode;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class GraphUtil {

    private GraphUtil() {
    }

    public final static Map<PathNode, Map> asGraph(final List<List<String>> links) {
        if (links == null) {
            return Collections.emptyMap();
        }
        final Map<PathNode, Map> map = Maps.newHashMap();
        for (final List<String> link : links) {
            GraphUtil.mergeInto(link, map);
        }
        return map;
    }

    private static void mergeInto(final List<String> list, final Map<PathNode, Map> map) {
        if (list.size() == 0) {
            return;
        }
        final String str = list.get(0);
        final PathNode node = PathNode.parse(str);
        Map<PathNode, Map> submap = map.get(node);
        if (submap == null) {
            submap = Maps.newHashMap();
            map.put(node, submap);
        }
        mergeInto(list.subList(1, list.size()), submap);
    }

}
