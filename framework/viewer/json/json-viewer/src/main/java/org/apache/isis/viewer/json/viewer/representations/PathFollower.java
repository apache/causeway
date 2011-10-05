package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public final class PathFollower {

    public final static Map<String,Map> asGraph(List<List<String>> links) {
        if(links == null) {
            return Collections.emptyMap();
        }
        final Map<String, Map> map = Maps.newHashMap();
        for (List<String> link : links) {
            mergeInto(link, map);
        }
        return map;
    }

    private static void mergeInto(List<String> list, Map<String, Map> map) {
        if(list.size() == 0) {
            return;
        }
        final String str = list.get(0);
        Map submap = map.get(str);
        if(submap == null) {
            submap = Maps.newHashMap(); 
            map.put(str, submap);
        }
        mergeInto(list.subList(1, list.size()), submap);
    }

    public final static PathFollower initial(List<List<String>> links) {
        final Map<String, Map> graph = asGraph(links);
        return new PathFollower(graph, Mode.FOLLOWING_PATH);
    }

    public final static PathFollower following(Map<String,Map> graph) {
        return new PathFollower(graph, Mode.FOLLOWING_PATH);
    }

    private static PathFollower terminated() {
        return new PathFollower(null, Mode.TERMINATED);
    }


    private enum Mode {
        FOLLOWING_PATH,
        TERMINATED;
    }

    private final Map<String, Map> graph;
    private Mode mode;

    private PathFollower(Map<String, Map> graph, Mode mode) {
        this.graph = graph;
        this.mode = mode;
    }

    /**
     * A little algebra...
     */
    public PathFollower follow(String path) {
        if(path == null || mode == Mode.TERMINATED) {
            return PathFollower.terminated();
        }
        if(mode == Mode.FOLLOWING_PATH) {
            Map remaining = graph.get(path);
            if(remaining != null) {
                return PathFollower.following(remaining);
            } else {
                return PathFollower.terminated();
            }
        }
        return PathFollower.terminated();
    }
    
    public boolean isTerminated() {
        return mode == Mode.TERMINATED;
    }

}
