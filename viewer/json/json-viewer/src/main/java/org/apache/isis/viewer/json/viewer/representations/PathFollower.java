package org.apache.isis.viewer.json.viewer.representations;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public final class PathFollower {

    public final static PathFollower initial(List<List<String>> followLinks) {
        final Map<String, List<String>> map = Maps.newHashMap();
        for (List<String> list : followLinks) {
            int size = list.size();
            if(size == 0) { continue; }
            String path = list.get(0);
            list.remove(0);
            map.put(path, list);
        }
        return new PathFollower(map, null, Mode.FINDING_PATH);
    }

    public final static PathFollower following(List<String> links) {
        return new PathFollower(null, links, Mode.FOLLOWING_PATH);
    }

    private static PathFollower terminated() {
        return new PathFollower(null, null, Mode.TERMINATED);
    }


    private enum Mode {
        FINDING_PATH,
        FOLLOWING_PATH,
        TERMINATED;
    }

    private final Map<String, List<String>> linkCandidatesByPath;
    private final List<String> links;
    private Mode mode;

    private PathFollower(Map<String, List<String>> linkCandidatesByPath, List<String> links, Mode mode) {
        this.linkCandidatesByPath = linkCandidatesByPath;
        this.links = links;
        this.mode = mode;
    }

    /**
     * A little algebra...
     */
    public PathFollower follow(String path) {
        if(path == null) {
            return PathFollower.terminated();
        }
        if(mode == Mode.FINDING_PATH) {
            List<String> remaining = linkCandidatesByPath.get(path);
            if(remaining != null) {
                return PathFollower.following(remaining);
            } else {
                return PathFollower.terminated();
            }
        }
        if(mode == Mode.FOLLOWING_PATH) {
            if(links.size() == 0) {
                return PathFollower.terminated();
            } else {
                String firstPath = links.get(0);
                if(Objects.equal(path, firstPath)) {
                    List<String> remaining = links.subList(1, links.size());
                    return PathFollower.following(remaining);
                } else {
                    return PathFollower.terminated();
                }
            }
        }
        return PathFollower.terminated();
    }
    
    public boolean isTerminated() {
        return mode == Mode.TERMINATED;
    }

}
