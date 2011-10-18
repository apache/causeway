package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.PathNode;



@SuppressWarnings({"rawtypes","unchecked"})
public final class LinkFollower {


    public final static LinkFollower create(List<List<String>> links) {
        final Map<PathNode, Map> graph = GraphUtil.asGraph(links);
        return new LinkFollower(graph, Mode.FOLLOWING, PathNode.NULL);
    }

    private enum Mode {
        FOLLOWING,
        TERMINATED;
    }

    private final Map<PathNode, Map> graph;
    private Mode mode;
    private final PathNode root;

    private LinkFollower(Map<PathNode, Map> graph, Mode mode, PathNode root) {
        this.graph = graph;
        this.mode = mode;
        this.root = root;
    }

    /**
     * A little algebra...
     */
    public LinkFollower follow(String path) {
        if(path == null) {
            return terminated(PathNode.NULL);
        }
        if(mode == Mode.TERMINATED) {
            return terminated(this.root);
        }
        PathNode node = PathNode.parse(path);
        if(mode == Mode.FOLLOWING) {
            Map<PathNode, Map> remaining = graph.get(node);
            if(remaining != null) {
                PathNode key = findKey(node);
                return new LinkFollower(remaining, Mode.FOLLOWING, key);
            } else {
                return terminated(node);
            }
        }
        return terminated(node);
    }

    /**
     * somewhat bizarre, but we have to find the actual node that is in the graph;
     * the one we matching on doesn't match on the {@link PathNode#getCriteria()} map.
     */
    private PathNode findKey(PathNode node) {
        final Set<PathNode> keySet = graph.keySet();
        for(PathNode key: keySet) {
            if(key.equals(node)) {
                return key;
            }
        }
        // shouldn't happen
        return node;
    }

    private static LinkFollower terminated(PathNode node) {
        return new LinkFollower(null, Mode.TERMINATED, node);
    }

    public boolean isFollowing() {
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
     * Any keys in the criterium that are not present in the map will be ignored.
     */
    public boolean matches(JsonRepresentation map) {
        if(!isFollowing()) {
            return false;
        }
        return root == null || root.matches(map);
    }


}
