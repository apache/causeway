package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

@SuppressWarnings({"rawtypes","unchecked"})
public final class ListUtil {
    
    private ListUtil(){}

    public final static Map<Node,Map> asGraph(List<List<String>> links) {
        if(links == null) {
            return Collections.emptyMap();
        }
        final Map<Node, Map> map = Maps.newHashMap();
        for (List<String> link : links) {
            ListUtil.mergeInto(link, map);
        }
        return map;
    }

    private static void mergeInto(List<String> list, Map<Node, Map> map) {
        if(list.size() == 0) {
            return;
        }
        final String str = list.get(0);
        final Node node = Node.parse(str);
        Map<Node,Map> submap = map.get(node);
        if(submap == null) {
            submap = Maps.newHashMap(); 
            map.put(node, submap);
        }
        mergeInto(list.subList(1, list.size()), submap);
    }

}
