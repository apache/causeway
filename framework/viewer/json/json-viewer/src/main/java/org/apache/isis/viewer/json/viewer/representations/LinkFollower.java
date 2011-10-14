package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.viewer.json.applib.JsonRepresentation;

import com.google.common.base.Objects;



@SuppressWarnings({"rawtypes","unchecked"})
public final class LinkFollower {

    public final static LinkFollower create(List<List<String>> links) {
        final Map<Node, Map> graph = ListUtil.asGraph(links);
        return new LinkFollower(graph, Mode.FOLLOWING, Node.NULL);
    }

    private enum Mode {
        FOLLOWING,
        TERMINATED;
    }

    private final Map<Node, Map> graph;
    private Mode mode;
    private final Node root;

    private LinkFollower(Map<Node, Map> graph, Mode mode, Node root) {
        this.graph = graph;
        this.mode = mode;
        this.root = root;
    }

    /**
     * A little algebra...
     */
    public LinkFollower follow(String path) {
        if(path == null) {
            return terminated(Node.NULL);
        }
        if(mode == Mode.TERMINATED) {
            return terminated(this.root);
        }
        Node node = Node.parse(path);
        if(mode == Mode.FOLLOWING) {
            Map<Node, Map> remaining = graph.get(node);
            if(remaining != null) {
                Node key = findKey(node);
                return new LinkFollower(remaining, Mode.FOLLOWING, key);
            } else {
                return terminated(node);
            }
        }
        return terminated(node);
    }

    /**
     * somewhat bizarre, but we have to find the actual node that is in the graph;
     * the one we matching on doesn't match on the {@link Node#getCriteria()} map.
     */
    private Node findKey(Node node) {
        final Set<Node> keySet = graph.keySet();
        for(Node key: keySet) {
            if(key.equals(node)) {
                return key;
            }
        }
        // shouldn't happen
        return node;
    }

    private static LinkFollower terminated(Node node) {
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
        for(Map.Entry<String,String> criterium: root.getCriteria().entrySet()) {
            final String requiredValue = criterium.getValue();
            final String actualValue = map.getString(criterium.getKey());
            if(!Objects.equal(requiredValue, actualValue)) {
                return false;
            }
        }
        return true;
    }

}
