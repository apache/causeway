package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

@SuppressWarnings({"rawtypes","unchecked"})
public final class LinkFollower {

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

    public final static LinkFollower initial(List<List<String>> links) {
        final Map<String, Map> graph = asGraph(links);
        return new LinkFollower(graph, Mode.FOLLOWINGPATH);
    }

    public final static LinkFollower following(Map<String,Map> graph) {
        return new LinkFollower(graph, Mode.FOLLOWINGPATH);
    }

    private static LinkFollower terminated() {
        return new LinkFollower(null, Mode.TERMINATED);
    }


    private enum Mode {
        FOLLOWINGPATH,
        TERMINATED;
    }

    private final Map<String, Map> graph;
    private Mode mode;

    private LinkFollower(Map<String, Map> graph, Mode mode) {
        this.graph = graph;
        this.mode = mode;
    }

    /**
     * A little algebra...
     */
    public LinkFollower follow(String path) {
        if(path == null || mode == Mode.TERMINATED) {
            return LinkFollower.terminated();
        }
        if(mode == Mode.FOLLOWINGPATH) {
            Map remaining = graph.get(path);
            if(remaining != null) {
                return LinkFollower.following(remaining);
            } else {
                return LinkFollower.terminated();
            }
        }
        return LinkFollower.terminated();
    }

    public boolean isFollowing() {
        return mode == Mode.FOLLOWINGPATH;
    }

    public boolean isTerminated() {
        return mode == Mode.TERMINATED;
    }

}
