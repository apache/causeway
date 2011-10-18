package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.viewer.json.applib.PathNode;

import com.google.common.collect.Maps;

@SuppressWarnings({"rawtypes","unchecked"})
public final class GraphUtil {
    
    private GraphUtil(){}

    public final static Map<PathNode,Map> asGraph(List<List<String>> links) {
        if(links == null) {
            return Collections.emptyMap();
        }
        final Map<PathNode, Map> map = Maps.newHashMap();
        for (List<String> link : links) {
            GraphUtil.mergeInto(link, map);
        }
        return map;
    }

    private static void mergeInto(List<String> list, Map<PathNode, Map> map) {
        if(list.size() == 0) {
            return;
        }
        final String str = list.get(0);
        final PathNode node = PathNode.parse(str);
        Map<PathNode,Map> submap = map.get(node);
        if(submap == null) {
            submap = Maps.newHashMap(); 
            map.put(node, submap);
        }
        mergeInto(list.subList(1, list.size()), submap);
    }

}
